/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory;

import coordinate.memory.nativememory.NativeInteger;
import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import java.util.Random;
import wrapper.core.CKernel;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import static wrapper.core.CNativeMemory.WRITE_ONLY;
import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author jmburu
 */
public class SimpleJOCL {
    public static void main(String... args)
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 10L;
        
        MemoryStruct<Int32> srcA = new MemoryStruct(new Int32(), globalSize, false); 
        MemoryStruct<Int32> srcB = new MemoryStruct(new Int32(), globalSize, false);
        MemoryStruct<Int32> dest = new MemoryStruct(new Int32(), globalSize, false);
        
        
        CNativeMemory<MemoryStruct<Int32>> nativeSrcA = configuration.createBufferNative(srcA, READ_ONLY);
        CNativeMemory<MemoryStruct<Int32>> nativeSrcB = configuration.createBufferNative(srcB, READ_ONLY);
        CNativeMemory<MemoryStruct<Int32>> nativeDest = configuration.createBufferNative(dest, WRITE_ONLY);
        
        Random rnd = new Random(System.currentTimeMillis());
        
       // nativeSrcA.transferToDevice();
       // nativeSrcB.transferToDevice();
                        
        nativeSrcA.write(n -> n.forEachSet((t, i)-> new Int32((int)i))); 
        nativeSrcB.write(n -> n.forEachSet((t, i) ->new Int32((int)i)));
       
        System.out.println(srcA);
        System.out.println(srcB);
        
        //execute kernel
        CKernel vectorAdd = configuration.createKernel("sampleKernel", nativeSrcA, nativeSrcB, nativeDest);
        configuration.execute1DKernel(vectorAdd, globalSize, 100);
        
        nativeDest.read(n -> System.out.println(n));
                
     //   nativeSrcA.write(n -> n.forEachSet((t, i) ->new Int32(rnd.nextInt(5))));   
      //  System.out.println(nativeSrcA.getT());
        
        configuration.execute1DKernel(vectorAdd, globalSize, 100);
        nativeDest.transferFromDevice();
     //   nativeDest.read(n -> System.out.println(n));
    }
}
