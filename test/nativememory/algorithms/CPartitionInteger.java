/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import static coordinate.utility.BitUtility.next_multipleof;
import coordinate.utility.RangeCheck;
import java.util.ArrayList;
import java.util.Collections;
import wrapper.core.CKernel;
import wrapper.core.CMemorySkeleton;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author jmburu
 */
public class CPartitionInteger 
{
    private final OpenCLConfiguration configuration; 
    private final CMemorySkeleton<Int32> values; 
    private final CMemorySkeleton<Int32> results; 
    private final long n; 
    private final CMemorySkeleton<Int32> flags;
    
    private boolean invoked = false;
    
    private final int   LOCALSIZE   = 64; //should be power of 2
    private final long  GLOBALSIZE;
    
    private final ArrayList<CMemorySkeleton> nativeMemories = new ArrayList();
    
    private int partitionIndex;
    
    public CPartitionInteger(
            OpenCLConfiguration configuration, 
            CMemorySkeleton<Int32> values, 
            CMemorySkeleton<Int32> result, 
            long n, 
            CMemorySkeleton<Int32> flags)
    {
        RangeCheck.rangeAboveZero(n);
        RangeCheck.checkBound(0, n, values.size());
        RangeCheck.checkBound(0, n, result.size());
        
        this.configuration = configuration;
        this.values = values;
        this.results = result;
        this.n = n;
        this.flags = flags;
        
        GLOBALSIZE  = next_multipleof(n, LOCALSIZE); 
    }
    
    public int execute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the sort has been invoked before, therefore create a new one");
        
        invoked = true;
        
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(n));
        
        CMemorySkeleton<Int32> stencil_1 = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> stencil_2 = configuration.createSVM(new Int32(), n);
        
        
        
        //kernel initialization
        CKernel transformOne            = configuration.createKernel("TransformIntToOne", flags, stencil_1, clength);
        CKernel transformOneReverse     = configuration.createKernel("TransformIntToOneReverse", stencil_1, stencil_2, clength);
        
        configuration.execute1DKernel(transformOne, GLOBALSIZE, LOCALSIZE);
        configuration.execute1DKernel(transformOneReverse, GLOBALSIZE, LOCALSIZE);
                
        int stencil1Value = stencil_1.readLast().value();
        int stencil2Value = stencil_2.readLast().value();
        
        int st1_total = new CPrefixSumInteger(configuration, stencil_1).execute() + stencil1Value;
        int st2_total = new CPrefixSumInteger(configuration, stencil_2).execute() + stencil2Value;   
        
        CMemorySkeleton<Int32> pIndex = configuration.createSVMValue(new Int32(st1_total));
                
        CKernel partitionInt = configuration.createKernel("PartitionInt", 
                values, results, stencil_1, stencil_2, flags, pIndex, clength);
        
        configuration.execute1DKernel(partitionInt, GLOBALSIZE, LOCALSIZE);
                        
        if((st1_total + st2_total) > n)
            throw new IndexOutOfBoundsException("Issue with partition");  
        
        Collections.addAll(nativeMemories, stencil_1, stencil_2, clength, pIndex);
        
        nativeMemories.forEach(struct -> {
            struct.free();
        });
        partitionIndex = st1_total;
        return st1_total;
    }
    
    public int partitionIndex()
    {
        return partitionIndex;
    }
}
