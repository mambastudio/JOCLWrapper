/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import static coordinate.utility.BitUtility.next_multipleof;
import java.util.ArrayList;
import java.util.Collections;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemoryLocal.LOCALINT;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public final class CLReduceInteger {
    
    private final OpenCLConfiguration configuration;    
    private final CNativeMemory<Int32> cdata;    
    private final long length;
    
    private CNativeMemory<Long64> clength;     
    private CNativeMemory<Int32>  cgroupReduce;       
    private CNativeMemory<Long64> cgroupSize;   
    private CNativeMemory<Int32>  cResult;   
    
    private CKernel localReduceIntegerKernel;  
    private CKernel groupReduceIntegerKernel;
    
    private final int LOCALSIZE   = 64; //should be power of 2
    private long GLOBALSIZE  = 0;
    private long GROUPSIZE   = 0;
    
    private boolean invoked = false;
    private boolean dataAlreadyTransfered = false;
    
    private final ArrayList<MemoryStruct> nativeMemories = new ArrayList();
    
    public CLReduceInteger(
            OpenCLConfiguration configuration, 
            CNativeMemory<Int32> data, 
            CNativeMemory<Int32> cresult, 
            boolean dataAlreadyTransfered)
    {
        this.dataAlreadyTransfered = dataAlreadyTransfered;
        this.configuration = configuration;
        this.cdata = data;        
        this.length = cdata.size();
        this.cResult = cresult;
        
        GLOBALSIZE  = next_multipleof(length, LOCALSIZE); 
        GROUPSIZE   = GLOBALSIZE/LOCALSIZE;
    }
    
    private void init()
    {
        initBuffers();
        initKernels();
    }
    
    private void initBuffers()
    {
        MemoryStruct<Long64> mlength        = new MemoryStruct(new Long64(length));   
        MemoryStruct<Int32>  mgroupReduce   = new MemoryStruct(new Int32(), GROUPSIZE, false);
        MemoryStruct<Long64> mgroupSize     = new MemoryStruct(new Long64(GROUPSIZE));
                
        Collections.addAll(nativeMemories, mlength, mgroupReduce, mgroupSize);
        
        clength           = configuration.createBufferNative(mlength, READ_WRITE);          
        cgroupReduce      = configuration.createBufferNative(mgroupReduce, READ_WRITE);       
        cgroupSize        = configuration.createBufferNative(mgroupSize, READ_WRITE); 
        
        if(!dataAlreadyTransfered)
        {
            this.cdata.transferToDevice();
            this.dataAlreadyTransfered = true;
        }
        this.clength.transferToDevice();
        this.cgroupSize.transferToDevice();
    }
    
    private void initKernels()
    {
        localReduceIntegerKernel      = configuration.createKernel("ReduceInt", cdata, cgroupReduce, clength, LOCALINT);
        groupReduceIntegerKernel      = configuration.createKernel("ReduceLoopInt", cgroupReduce, cgroupSize, cResult);
    }
    
    private void kernelExecute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the prefix sum has been invoked before, therefore create a new one");
        
        //start doing a local (workgroup) byteCapacity scan
        configuration.execute1DKernel(localReduceIntegerKernel, GLOBALSIZE, LOCALSIZE); 
        
        //tree execution (if groupsum is quite large, therefore local scan suffices)
        if(GROUPSIZE > 1<<18)
        {            
            CLReduceInteger reduce = new CLReduceInteger(configuration, cgroupReduce, cResult, true);
            reduce.execute();
        }
        else //do serial scan
            configuration.execute1DKernel(groupReduceIntegerKernel, 1, 1); //Do group level scan
        
        configuration.finish();
        
        invoked = true;
            
        for(MemoryStruct struct: nativeMemories)
            struct.dispose();
    }
    
    //called once
    public void execute()
    {
        init();
        kernelExecute();
    }
    
    public CNativeMemory<Int32> cResult()
    {
        return cResult;
    }
}
