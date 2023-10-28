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
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CNativeMemory;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class CLReduceInteger {
    
    private final OpenCLConfiguration configuration;    
    private final CNativeMemory<MemoryStruct<Int32>> cdata;    
    private final long length;
    
    private CNativeMemory<MemoryStruct<Long64>> clength;     
    private CNativeMemory<MemoryStruct<Int32>> cgroupSum;       
    private CNativeMemory<MemoryStruct<Long64>> cgroupSize;   
    
    private final int LOCALSIZE   = 64; //should be power of 2
    private long GLOBALSIZE  = 0;
    private long GROUPSIZE   = 0;
    
    private boolean invoked = false;
    private boolean dataAlreadyTransfered = false;
    
    private final ArrayList<MemoryStruct> nativeMemories = new ArrayList();
    
    public CLReduceInteger(OpenCLConfiguration configuration, MemoryStruct<Int32> data)
    {
        this.configuration = configuration;
        this.cdata = configuration.createBufferNative(data, READ_WRITE);        
        this.length = cdata.size();
        
        GLOBALSIZE  = next_multipleof(length, LOCALSIZE); 
        GROUPSIZE   = GLOBALSIZE/LOCALSIZE;    
    }
    
    public CLReduceInteger(OpenCLConfiguration configuration, CNativeMemory<MemoryStruct<Int32>> data, boolean dataAlreadyTransfered)
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
        
    }
    
    private void kernelExecute()
    {
        
    }
    
    //called once
    public void execute()
    {
        init();
        kernelExecute();
    }
}
