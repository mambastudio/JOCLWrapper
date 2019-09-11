/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Arrays;
import java.util.Random;
import wrapper.core.CBufferFactory;
import wrapper.core.CCommandQueue;
import wrapper.core.CConfiguration;
import wrapper.core.CContext;
import wrapper.core.CDevice;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.WRITE_ONLY;
import wrapper.core.CPlatform;
import wrapper.core.CProgram;
import wrapper.core.CResourceFactory;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class HelloJOCL {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDefaultDevice();        
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(CLFileReader.readFile("C:\\Users\\user\\Documents\\Java\\jocl\\cl", "HelloCL.cl"));
        CCommandQueue queue = context.createCommandQueue(device);
        
        int globalSize = 10;
        
        CFloatBuffer aBuffer = CBufferFactory.allocFloat("abuffer", context, globalSize, READ_ONLY);
        CFloatBuffer bBuffer = CBufferFactory.allocFloat("bbuffer", context, globalSize, READ_ONLY);
        CFloatBuffer cBuffer = CBufferFactory.allocFloat("cbuffer", context, globalSize, WRITE_ONLY);  
        
        aBuffer.mapWriteBuffer(queue, buffer -> {
            Random rnd = new Random(System.currentTimeMillis());
            while(buffer.remaining() != 0)
                buffer.put(rnd.nextFloat()*100);            
        });        
        bBuffer.mapWriteBuffer(queue, buffer -> {
            Random rnd = new Random(System.currentTimeMillis());
            while(buffer.remaining() != 0)
                buffer.put(rnd.nextFloat()*100);            
        });
        
        
        CKernel vectorAdd = program.createKernel("VectorAdd");
        vectorAdd.putArgs(aBuffer, bBuffer, cBuffer);
        
        queue.put1DRangeKernel(vectorAdd, globalSize, globalSize);        
        
        System.out.println(Arrays.toString(cBuffer.getArray(queue)));
        //cBuffer.mapReadBuffer(queue, buffer -> System.out.println(cBuffer));        
        CResourceFactory.releaseAll();
      
    }    
}
