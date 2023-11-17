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
 * @author jmburu
 */
public class CReduceInteger {
    private final OpenCLConfiguration configuration;    
    private final CMemorySkeleton<Int32> cdata;  
    private final CMemorySkeleton<Int32>  cResult;   
    
    private CMemorySkeleton<Long64> clength;     
    private CMemorySkeleton<Int32>  cgroupReduce;       
    private CMemorySkeleton<Long64> cgroupSize;   
    
    
    private CKernel localReduceIntegerKernel;  
    private CKernel groupReduceIntegerKernel;
    
    private final int LOCALSIZE   = 64; //should be power of 2
    private long GLOBALSIZE  = 0;
    private long GROUPSIZE   = 0;
    
    private final CMemorySkeleton<Int32> LOCALINT = new SVMNative(new Int32());
    
    private boolean invoked = false;
    
    private final ArrayList<CMemorySkeleton> nativeMemories = new ArrayList();
    
    public CReduceInteger(
            OpenCLConfiguration configuration, 
            CMemorySkeleton<Int32> data, 
            CMemorySkeleton<Int32> cresult)
    {
        this.configuration = configuration;
        this.cdata = data;      
        this.cResult = cresult;
        
        GLOBALSIZE  = next_multipleof(cdata.size(), LOCALSIZE); 
        GROUPSIZE   = GLOBALSIZE/LOCALSIZE;
    }
    
    private void init()
    {
        initBuffers();
        initKernels();
    }
    
    private void initBuffers()
    {                       
        
        clength           = configuration.createSVMValue(new Long64(cdata.size()));          
        cgroupReduce      = configuration.createSVM(new Int32(), GROUPSIZE);       
        cgroupSize        = configuration.createSVMValue(new Long64(GROUPSIZE));   
        
        Collections.addAll(nativeMemories, clength, cgroupReduce, cgroupSize);
    }
    
    private void initKernels()
    {
        localReduceIntegerKernel      = configuration.createKernel("ReduceInt", cdata, cgroupReduce, clength, LOCALINT);
        groupReduceIntegerKernel      = configuration.createKernel("ReduceLoopInt", cgroupReduce, cgroupSize, cResult);
    }
    
    private void kernelExecute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the reduce function has been invoked before, therefore create a new one");
        
        //start doing a local (workgroup) byteCapacity scan
        configuration.execute1DKernel(localReduceIntegerKernel, GLOBALSIZE, LOCALSIZE); 
        
        //tree execution (if groupsum is quite large, therefore local scan suffices)
        if(GROUPSIZE > 1<<18)
        {            
            CReduceInteger reduce = new CReduceInteger(configuration, cgroupReduce, cResult);
            reduce.execute();
        }
        else //do serial scan
            configuration.execute1DKernel(groupReduceIntegerKernel, 1, 1); //Do group level scan
        
        configuration.finish();
        
        invoked = true;
            
        nativeMemories.forEach(struct -> {
            struct.free();
        });
    }
    
    //called once
    public void execute()
    {
        init();
        kernelExecute();
    }    
}
