/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory;

import coordinate.memory.nativememory.NativeInteger;
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
        
        //global size
        int globalSize = 10;
        
        NativeInteger srcA = new NativeInteger(globalSize);
        NativeInteger srcB = new NativeInteger(globalSize);
        NativeInteger dest = new NativeInteger(globalSize);
        
        CNativeMemory<NativeInteger> nativeSrcA = configuration.createBufferNative(srcA, READ_ONLY);
        CNativeMemory<NativeInteger> nativeSrcB = configuration.createBufferNative(srcB, READ_ONLY);
        CNativeMemory<NativeInteger> nativeDest = configuration.createBufferNative(dest, WRITE_ONLY);
        
        Random rnd = new Random(System.currentTimeMillis());
                        
        nativeSrcA.write(n -> n.iterateRange(i ->(int)i));
        nativeSrcB.write(n -> n.iterateRange(i ->(int)i));
       
        System.out.println(srcA);
        System.out.println(srcB);
        
        //execute kernel
        CKernel vectorAdd = configuration.createKernel("sampleKernel", nativeSrcA, nativeSrcB, nativeDest);
        configuration.execute1DKernel(vectorAdd, globalSize, globalSize);
        
        nativeDest.read(n -> System.out.println(n));
                
        nativeSrcA.write(n -> n.iterateRange(i-> rnd.nextInt(5)));   
        System.out.println(nativeSrcA.getT());
        
        configuration.execute1DKernel(vectorAdd, globalSize, globalSize);
        nativeDest.read(n -> System.out.println(n));
    }
}
