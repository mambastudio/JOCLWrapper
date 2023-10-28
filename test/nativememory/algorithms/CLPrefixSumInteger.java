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
public class CLPrefixSumInteger 
{
    private final OpenCLConfiguration configuration;    
    private final CNativeMemory<MemoryStruct<Int32>> cdata;    
    private final long length;
    
    private CNativeMemory<MemoryStruct<Long64>> clength;     
    private CNativeMemory<MemoryStruct<Int32>> cgroupSum;       
    private CNativeMemory<MemoryStruct<Long64>> cgroupSize;   
    
    private CKernel localScanIntegerKernel;  
    private CKernel groupScanIntegerKernel;  
    private CKernel globalScanIntegerKernel;
    
    private final int LOCALSIZE   = 64; //should be power of 2
    private long GLOBALSIZE  = 0;
    private long GROUPSIZE   = 0;
    
    private boolean invoked = false;
    private boolean dataAlreadyTransfered = false;
    
    private final ArrayList<MemoryStruct> nativeMemories = new ArrayList();
    
    public CLPrefixSumInteger(OpenCLConfiguration configuration, MemoryStruct<Int32> data)
    {
        
        this.configuration = configuration;
        this.cdata = configuration.createBufferNative(data, READ_WRITE);        
        this.length = cdata.size();
        
        GLOBALSIZE  = next_multipleof(length, LOCALSIZE); 
        GROUPSIZE   = GLOBALSIZE/LOCALSIZE;    
    }
    
    public CLPrefixSumInteger(OpenCLConfiguration configuration, CNativeMemory<MemoryStruct<Int32>> data, boolean dataAlreadyTransfered)
    {
        this.dataAlreadyTransfered = dataAlreadyTransfered;
        this.configuration = configuration;
        this.cdata = data;        
        this.length = cdata.size();
        
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
        MemoryStruct<Long64> mlength = new MemoryStruct(new Long64(length));   
        MemoryStruct<Int32> mgroupSum = new MemoryStruct(new Int32(), GROUPSIZE, false);
        MemoryStruct<Long64> mgroupSize = new MemoryStruct(new Long64(GROUPSIZE));
                
        Collections.addAll(nativeMemories, mlength, mgroupSum, mgroupSize);
        
        clength           = configuration.createBufferNative(mlength, READ_WRITE);          
        cgroupSum         = configuration.createBufferNative(mgroupSum, READ_WRITE);       
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
        localScanIntegerKernel      = configuration.createKernel("LocalScanInteger", cdata, cdata, cgroupSum, clength, LOCALINT);
        groupScanIntegerKernel      = configuration.createKernel("GroupScanInteger", cgroupSum, cgroupSize);
        globalScanIntegerKernel     = configuration.createKernel("LocalScanToGlobalInteger", cdata, cgroupSum, clength);
    }
    
    private void kernelExecute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the prefix sum has been invoked before, therefore create a new one");
        
        //these three kernels would provide a prefix sum that is quite fast for a huge array
        configuration.execute1DKernel(localScanIntegerKernel, GLOBALSIZE, LOCALSIZE); //start doing a local (workgroup) byteCapacity scan
        
        //tree execution (if groupsum is quite large, therefore local scan suffices)
        if(GROUPSIZE > 1<<18)
        {            
            CLPrefixSumInteger prefixSum = new CLPrefixSumInteger(configuration, cgroupSum, true);
            prefixSum.execute();
        }
        else //do serial scan
            configuration.execute1DKernel(groupScanIntegerKernel, 1, 1); //Do group level scan
        
        configuration.execute1DKernel(globalScanIntegerKernel, GLOBALSIZE, LOCALSIZE); //add the group level scan to the local byteCapacity scan
        configuration.finish();
        
        for(MemoryStruct struct: nativeMemories)
            struct.dispose();
              
        invoked = true;
    }
    
    //called once
    public void execute()
    {
        init();
        kernelExecute();
    }    
}
