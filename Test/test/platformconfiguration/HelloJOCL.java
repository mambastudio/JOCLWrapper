/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.platformconfiguration;

import java.util.Random;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.WRITE_ONLY;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class HelloJOCL {
    public static void main(String... args)
    {
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile("C:\\Users\\user\\Documents\\Java\\jocl\\cl", "HelloCL.cl"));
        
        int globalSize = 10;
        
        CFloatBuffer aBuffer = configuration.allocFloat("abuffer", globalSize, READ_ONLY);
        CFloatBuffer bBuffer = configuration.allocFloat("bbuffer", globalSize, READ_ONLY);
        CFloatBuffer cBuffer = configuration.allocFloat("cbuffer", globalSize, WRITE_ONLY);  
        
        configuration.mapWriteBuffer(aBuffer, buffer -> {
            Random rnd = new Random(System.currentTimeMillis());
            while(buffer.remaining() != 0)
                buffer.put(rnd.nextFloat()*100);            
        });
        
        configuration.mapWriteBuffer(bBuffer, buffer -> {
            Random rnd = new Random(System.currentTimeMillis());
            while(buffer.remaining() != 0)
                buffer.put(rnd.nextFloat()*100);            
        });
        
        CKernel kernel = configuration.createKernel("VectorAdd", aBuffer, bBuffer, cBuffer);        
        configuration.executeKernel1D(kernel, globalSize, globalSize);
        
        configuration.mapReadBuffer(cBuffer, buffer -> System.out.println(cBuffer));        
        CResourceFactory.releaseAll();
    }
    
}
