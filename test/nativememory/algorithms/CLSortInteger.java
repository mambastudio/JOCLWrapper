/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Float32;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import static coordinate.utility.BitUtility.next_log2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class CLSortInteger {
    private final OpenCLConfiguration configuration;
    private final CNativeMemory<Int32> cdata;    
    private final long length;
    
    private boolean invoked = false;
    private boolean dataAlreadyTransfered = false;
    
    private final ArrayList<MemoryStruct> nativeMemories = new ArrayList();
    
    public CLSortInteger(OpenCLConfiguration configuration, MemoryStruct<Int32> data)
    {
        this.configuration = configuration;
        this.cdata = configuration.createBufferNative(data, READ_WRITE);        
        this.length = cdata.size();   
    }
    
    public CLSortInteger(OpenCLConfiguration configuration, CNativeMemory<Int32> data, boolean dataAlreadyTransfered)
    {
        this.dataAlreadyTransfered = dataAlreadyTransfered;
        this.configuration = configuration;
        this.cdata = data;        
        this.length = cdata.size();
    }
    
    public void execute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the sort has been invoked before, therefore create a new one");
        //start of sort structure
        int radix  = 2;  
        int until = (int) next_log2(length);
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;
                
        int globalSize = T;
        int localSize = globalSize<64 ? globalSize : 64;
                
        MemoryStruct<Long64> mlength = new MemoryStruct(new Long64(length));     
        MemoryStruct<Float32>  mpowerx  = new MemoryStruct(new Float32());        
        CNativeMemory<Long64> clength     = configuration.createBufferNative(mlength, READ_ONLY);
        CNativeMemory<Float32> cpowerx    = configuration.createBufferNative(mpowerx, READ_WRITE);
        
        clength.transferToDevice(); //this is very very important
        
        Collections.addAll(nativeMemories, mlength, mpowerx);
        
        if(!dataAlreadyTransfered)
        {
            this.cdata.transferToDevice();
            this.dataAlreadyTransfered = true;
        }
        
        //kernel initialization
        CKernel cbutterfly1    = configuration.createKernel("Butterfly1_Int", cdata, clength, cpowerx);
        CKernel cbutterfly2    = configuration.createKernel("Butterfly2_Int", cdata, clength, cpowerx);
    
        AtomicInteger xout = new AtomicInteger();
        for(xout.set(1); xout.get()<=until; xout.getAndIncrement())
        {     
            cpowerx.write(v-> v.set(0, new Float32((float)Math.pow(radix, xout.get())))); //PowerX = (Math.pow(radix, xout)); 
                                                
            // OpenCL kernel call
            configuration.execute1DKernel(cbutterfly1, globalSize, localSize);
            
            if(xout.get() > 1)
            {                
                AtomicInteger xin = new AtomicInteger();
                for(xin.set(xout.get()); xin.get() > 0; xin.getAndDecrement())
                {                     
                    cpowerx.write(v->v.set(0,new Float32((float)Math.pow(radix, xin.get()))));                    
                    // OpenCL kernel call
                    configuration.execute1DKernel(cbutterfly2, globalSize, localSize);                     
                }
            }
        }
        
        for(MemoryStruct struct: nativeMemories)
            struct.dispose();
        invoked = true;        
    }
}
