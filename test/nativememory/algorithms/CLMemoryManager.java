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
import java.util.Objects;
import wrapper.core.CKernel;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import static wrapper.core.CNativeMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class CLMemoryManager {
    public static OpenCLConfiguration configuration = null; 
    
    public static void fillOne(CNativeMemory<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CNativeMemory<Long64> clength = configuration.createBufferNative(new MemoryStruct(new Long64(cmem.size())), READ_ONLY);
        clength.transferToDevice(); //always remember to transfer to device
        
        CKernel initArrayIntOneKernel = configuration.createKernel("InitArrayIntOne", cmem, clength);       
        configuration.execute1DKernel(initArrayIntOneKernel, GLOBALSIZE, LOCALSIZE);
        configuration.finish();        
    }
    
    public static CNativeMemory<Int32> fillOne(MemoryStruct<Int32> mem)
    {
        CNativeMemory<Int32> cmem = configuration.createBufferNative(mem, READ_WRITE);
        fillOne(cmem);
        return cmem;
    }    
    
    public static CNativeMemory<Int32> fillOne(long size)
    {
        MemoryStruct<Int32> mem = new MemoryStruct(new Int32(), size, false);
        return fillOne(mem);
    }  
    
    public static void fillIndexReverse(CNativeMemory<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CNativeMemory<Long64> clength = configuration.createBufferNative(new MemoryStruct(new Long64(cmem.size())), READ_ONLY);
        clength.transferToDevice(); //always remember to transfer to device
        
        CKernel initArrayIntOneKernel = configuration.createKernel("InitArrayIntIndexReverse", cmem, clength);       
        configuration.execute1DKernel(initArrayIntOneKernel, GLOBALSIZE, LOCALSIZE);
        configuration.finish();        
    }
    
    public static CNativeMemory<Int32> fillIndexReverse(MemoryStruct<Int32> mem)
    {
        CNativeMemory<Int32> cmem = configuration.createBufferNative(mem, READ_WRITE);
        fillIndexReverse(cmem);
        return cmem;
    }    
    
    public static CNativeMemory<Int32> fillIndexReverse(long size)
    {
        MemoryStruct<Int32> mem = new MemoryStruct(new Int32(), size, false);
        return fillIndexReverse(mem);
    }  
}
