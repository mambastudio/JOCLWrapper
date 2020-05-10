/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.time.Duration;
import java.time.Instant;
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
public class KoggStoneScan {
    public static void main(String... args)
    {
        int globalSize = 10;
        int localSize = 5;
        
        CL.setExceptionsEnabled(true);
       
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(StreamScan.class, "KoggStoneScan.cl"));
        
        CIntBuffer inputBuffer = configuration.allocInt("inputBuffer", globalSize, READ_WRITE); 
        CIntBuffer outputBuffer = configuration.allocInt("outputBuffer", globalSize, READ_WRITE);
        //CIntBuffer tempBuffer = configuration.allocInt("tempBuffer", localSize, READ_WRITE);
        
        inputBuffer.mapWriteBuffer(configuration.queue(), buffer->{
            Random random = new Random();
            buffer.put(random.ints(globalSize, 1, 2).toArray());  
            System.out.println(Arrays.toString(buffer.array()));
        });
        long time1 = System.nanoTime();
        CKernel koggeStoneKernel = configuration.createKernel("koggeStone", inputBuffer, outputBuffer, LOCALINT);
        configuration.executeKernel1D(koggeStoneKernel, globalSize, localSize);
        long time2 = System.nanoTime();
        double mTime = (double)(time2 - time1)/1_000_000_000;
        System.out.printf("%.12f \n", mTime);
        
        outputBuffer.transferFromDeviceToBuffer(configuration.queue());
        System.out.println(Arrays.toString(outputBuffer.getBuffer().array()));
    }
}
