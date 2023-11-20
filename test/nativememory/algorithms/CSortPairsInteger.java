/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStructFactory.Float32;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import static coordinate.utility.BitUtility.next_log2;
import coordinate.utility.RangeCheck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import wrapper.core.CKernel;
import wrapper.core.CMemorySkeleton;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class CSortPairsInteger {
    private final OpenCLConfiguration configuration;
    private final CMemorySkeleton<Int32> ckeys;    
    private final CMemorySkeleton<Int32> cvalues;    
    private final CMemorySkeleton<Int32> ckeysOut;    
    private final CMemorySkeleton<Int32> cvaluesOut;  
    
    private final long n;
    
    private boolean invoked = false;
    
    private final ArrayList<CMemorySkeleton> nativeMemories = new ArrayList();
    
    public CSortPairsInteger(
            OpenCLConfiguration configuration, 
            CMemorySkeleton<Int32> ckeys, CMemorySkeleton<Int32> cvalues,
            CMemorySkeleton<Int32> ckeysOut, CMemorySkeleton<Int32> cvaluesOut,
            long n)
    {
        RangeCheck.checkBound(0, ckeys.size(), n);
        RangeCheck.checkBound(0, cvalues.size(), n);
        RangeCheck.checkBound(0, ckeysOut.size(), n);
        RangeCheck.checkBound(0, cvaluesOut.size(), n);
        
        this.configuration = configuration;
        this.ckeys = ckeys;  
        this.cvalues = cvalues;
        this.ckeysOut = ckeysOut;  
        this.cvaluesOut = cvaluesOut;   
        this.n = n;
    }
    
    public void execute()
    {
        if(invoked)
            throw new UnsupportedOperationException("the sort has been invoked before, therefore create a new one");
        
        //transfer copy to memory  
        CLMemoryManager.copyMemory(ckeys, ckeysOut, n);
        CLMemoryManager.copyMemory(cvalues, cvaluesOut, n);
        
        //start of sort structure
        int radix  = 2;  
        int until = (int) next_log2(n);
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;
                
        int globalSize = T;
        int localSize = globalSize<64 ? globalSize : 64;
                
        CMemorySkeleton<Long64> clength     = configuration.createSVMValue(new Long64(n));
        CMemorySkeleton<Float32> cpowerx    = configuration.createSVMValue(new Float32());
                
        Collections.addAll(nativeMemories, clength, cpowerx);
        
        //kernel initialization
        CKernel cbutterfly1    = configuration.createKernel("Butterfly1_IntPair", ckeysOut, cvaluesOut, clength, cpowerx);
        CKernel cbutterfly2    = configuration.createKernel("Butterfly2_IntPair", ckeysOut, cvaluesOut, clength, cpowerx);
    
        AtomicInteger xout = new AtomicInteger();
        for(xout.set(1); xout.get()<=until; xout.getAndIncrement())
        {     
            cpowerx.write(new Float32((float)Math.pow(radix, xout.get()))); //PowerX = (Math.pow(radix, xout)); 
                                                
            // OpenCL kernel call
            configuration.execute1DKernel(cbutterfly1, globalSize, localSize);
            
            if(xout.get() > 1)
            {                
                AtomicInteger xin = new AtomicInteger();
                for(xin.set(xout.get()); xin.get() > 0; xin.getAndDecrement())
                {                     
                    cpowerx.write(new Float32((float)Math.pow(radix, xin.get())));                    
                    // OpenCL kernel call
                    configuration.execute1DKernel(cbutterfly2, globalSize, localSize);                     
                }
            }
        }
        
        nativeMemories.forEach(struct -> {
            struct.free();
        });
        invoked = true;        
    }
}
