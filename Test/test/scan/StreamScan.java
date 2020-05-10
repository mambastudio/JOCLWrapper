/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import static wrapper.core.CBufferMemory.LOCALINT;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class StreamScan {
    public static void main(String... args)
    {
        int globalSize = 24;
        int localSize = 8;
        
        CL.setExceptionsEnabled(true);
       
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(StreamScan.class, "StreamScan.cl"));
        
        CIntBuffer inputBuffer = configuration.allocInt("floatBuffer", globalSize, READ_WRITE); 
        inputBuffer.mapWriteBuffer(configuration.queue(), buffer->{
            Random random = new Random();
            buffer.put(random.ints(globalSize, 0, 2).toArray());  
            System.out.println(Arrays.toString(buffer.array()));
        });
        CIntBuffer intermediateBuffer = configuration.allocInt("intermediateBuffer", globalSize/localSize, READ_WRITE); 
        
        CKernel streamScan = configuration.createKernel("StreamScan");
        streamScan.putArgs(inputBuffer, intermediateBuffer, LOCALINT, LOCALINT);
        
        configuration.queue().put1DRangeKernel(streamScan, globalSize, localSize);
        
        inputBuffer.mapReadBuffer(configuration.queue(), buffer->{
            System.out.println(Arrays.toString(buffer.array()));
        });
        
    }
}
