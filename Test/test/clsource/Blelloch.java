/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.clsource;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CBufferMemory;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class Blelloch {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(Blelloch.class, "Blelloch.cl"));
        int length                  = 8;
        
        CIntBuffer inputBuffer       = configuration.allocInt("inputBuffer", length, READ_WRITE);  
        CIntBuffer outputBuffer      = configuration.allocInt("outputBuffer", length, READ_WRITE);
        
        inputBuffer.mapWriteBuffer(configuration.queue(), buffer->
        {
            Random r = new Random();
            while(buffer.hasRemaining())
                buffer.put(r.ints(length, 0, 2).limit(1).findFirst().getAsInt());
            System.out.println(Arrays.toString(buffer.array()));
        });
        
        CKernel blellochKernel = configuration.createKernel("blelloch_scan", inputBuffer, outputBuffer, CBufferMemory.LOCALINT);
        configuration.executeKernel1D(blellochKernel, length, length);
        
        outputBuffer.mapReadBuffer(configuration.queue(), buffer->
        {            
            System.out.println(Arrays.toString(Arrays.copyOf(buffer.array(), length)));
        });
    }
}
