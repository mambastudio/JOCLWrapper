/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class SAM {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);       
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(StreamScan.class, "SAM.cl"));
        
        int global                  = 16;     
        int local                   = 4;
        
        CIntBuffer input      = configuration.allocInt("input", global, READ_WRITE);
        CIntBuffer output     = configuration.allocInt("output", global, READ_WRITE);
        CIntBuffer sums       = configuration.allocInt("sums", global/local, READ_WRITE);
        CIntBuffer flags      = configuration.allocInt("flags", global/local, READ_WRITE);
        
        CKernel samKernel = configuration.createKernel("test");
        samKernel.putArgs(input, output, sums, flags);
        
        Random r = new Random();
        input.mapWriteBuffer(configuration.queue(), buffer->{
            int[] inputArray = buffer.array();
            for(int i = 0; i<inputArray.length; i++)
                inputArray[i] = (r.ints(global, 0, 4).limit(1).findFirst().getAsInt());
            System.out.println(Arrays.toString(inputArray));
        });
        
        flags.mapWriteBuffer(configuration.queue(), buffer->{
            int[] flagsArray = buffer.array();
            for(int i = 0; i<flagsArray.length; i++)
                flagsArray[i] = 0;
            System.out.println(Arrays.toString(flagsArray));
        });
        
        configuration.executeKernel1D(samKernel, global, local);
        
        sums.mapReadBuffer(configuration.queue(), buffer->{
            int[] sumsArray = buffer.array();
            System.out.println(Arrays.toString(sumsArray));
        });
        
        output.mapReadBuffer(configuration.queue(), buffer->{
            int[] outputArray = buffer.array();
            System.out.println(Arrays.toString(outputArray));
        });
    }
}
