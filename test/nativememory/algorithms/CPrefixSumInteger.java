/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import static coordinate.utility.BitUtility.next_multipleof;
import java.util.ArrayList;
import java.util.Collections;
import wrapper.core.CKernel;
import wrapper.core.CMemorySkeleton;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.SVMNative;

/**
 *
 * @author user
 */
public class CPrefixSumInteger 
{
    private final OpenCLConfiguration configuration;    
    private final CMemorySkeleton<Int32> cdata;    
    private final long length;
    
    private CMemorySkeleton<Long64> clength;     
    private CMemorySkeleton<Int32> cgroupSum;       
    private CMemorySkeleton<Long64> cgroupSize;   
    
    private CKernel localScanIntegerKernel;  
    private CKernel groupScanIntegerKernel;  
    private CKernel globalScanIntegerKernel;
    
    private final CMemorySkeleton<Int32> LOCALINT = new SVMNative(new Int32());
    
    private final int LOCALSIZE   = 64; //should be power of 2
    private long GLOBALSIZE  = 0;
    private long GROUPSIZE   = 0;
    
    private boolean invoked = false;    
    private final ArrayList<CMemorySkeleton> nativeMemories = new ArrayList();
           
    public CPrefixSumInteger(OpenCLConfiguration configuration, CMemorySkeleton<Int32> data)
    {
        this.configuration = configuration;
        this.cdata = data;        
        this.length = cdata.size();
        
        GLOBALSIZE  = next_multipleof(length, LOCALSIZE); 
        GROUPSIZE   = GLOBALSIZE/LOCALSIZE; //if actual length is less than local size, group size is 1
    }
        
    private void init()
    {
        initBuffers();
        initKernels();
    }
    
    private void initBuffers()
    {                       
        clength           = configuration.createSVMValue(new Long64(length));           
        cgroupSum         = configuration.createSVM(new Int32(), GROUPSIZE);       
        cgroupSize        = configuration.createSVMValue(new Long64(GROUPSIZE));  
        
        Collections.addAll(nativeMemories, clength, cgroupSum, cgroupSize);       
    }
    
    private void initKernels()
    {
        localScanIntegerKernel      = configuration.createKernel("LocalScanInteger", cdata, cdata, cgroupSum, clength, LOCALINT);
        groupScanIntegerKernel      = configuration.createKernel("GroupScanInteger", cgroupSum, cgroupSize);
        globalScanIntegerKernel     = configuration.createKernel("LocalScanToGlobalInteger", cdata, cgroupSum, clength);
    }
    
    private void kernelExecute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the prefix sum has been invoked before, therefore create a new one");
        
        //start doing a local (workgroup) byteCapacity scan
        configuration.execute1DKernel(localScanIntegerKernel, GLOBALSIZE, LOCALSIZE); 
        
        //tree execution (if groupsum is quite large, therefore local scan suffices)
        if(GROUPSIZE > 1<<18)
        {            
            CPrefixSumInteger prefixSum = new CPrefixSumInteger(configuration, cgroupSum);
            prefixSum.execute();
        }
        else //do serial scan
            configuration.execute1DKernel(groupScanIntegerKernel, 1, 1); //Do group level scan
        
        //add the group level scan to the local byteCapacity scan
        configuration.execute1DKernel(globalScanIntegerKernel, GLOBALSIZE, LOCALSIZE);         
        
        configuration.finish();
        
        nativeMemories.forEach(struct -> {
            struct.free();
        });
              
        invoked = true;
    }
    
    //called once
    public int execute()
    {
        init();
        kernelExecute();
        return sum();
    }    
    
    public int sum()
    {
        return cdata.readLast().value();
    }
}
