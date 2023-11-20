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
import coordinate.utility.RangeCheck;
import java.util.Objects;
import wrapper.core.CKernel;
import wrapper.core.CMemorySkeleton;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import static wrapper.core.CNativeMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.SVMNative;

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
    
    public static void fillOne(CMemorySkeleton<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(cmem.size()));
        
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
    
    public static void fillIndexReverse(CMemorySkeleton<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(cmem.size()));
        
        CKernel initArrayIntOneKernel = configuration.createKernel("InitArrayIntIndexReverse", cmem, clength);       
        configuration.execute1DKernel(initArrayIntOneKernel, GLOBALSIZE, LOCALSIZE);
        configuration.finish(); 
        clength.free();
    }
    
    public static void fillIndex(CMemorySkeleton<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(cmem.size()));
        
        CKernel initArrayIntOneKernel = configuration.createKernel("InitArrayIntIndex", cmem, clength);       
        configuration.execute1DKernel(initArrayIntOneKernel, GLOBALSIZE, LOCALSIZE);
        configuration.finish();   
        clength.free();
    }
    
    public static void fillIndexInclusive(CMemorySkeleton<Int32> cmem)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(cmem, "memory is null");
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(cmem.size(), LOCALSIZE); 
       
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(cmem.size()));
        
        CKernel initArrayIntOneKernel = configuration.createKernel("InitArrayIntIndexInclusive", cmem, clength);       
        configuration.execute1DKernel(initArrayIntOneKernel, GLOBALSIZE, LOCALSIZE);
        configuration.finish();    
        clength.free();
    }
    
    public static void copyMemory(CMemorySkeleton<Int32> in, CMemorySkeleton<Int32> out, long n)
    {
        Objects.requireNonNull(configuration, "opencl configuration is null");
        Objects.requireNonNull(in, "memory is null");
        Objects.requireNonNull(out, "memory is null");
        
        RangeCheck.checkBound(0, in.size(), n);
        RangeCheck.checkBound(0, out.size(), n);
        
        long  LOCALSIZE     = 64;
        long  GLOBALSIZE    = next_multipleof(n, LOCALSIZE); 
       
        CMemorySkeleton<Long64> clength = configuration.createSVMValue(new Long64(n));
        
        CKernel copyTo = configuration.createKernel("CopyTo", in, out, clength);       
        configuration.execute1DKernel(copyTo, GLOBALSIZE, LOCALSIZE);
        configuration.finish(); 
        clength.free();
    }
}
