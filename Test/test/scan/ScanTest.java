/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.util.CLFileReader;

/**
 * @author user
 
 */
public class ScanTest { 
    
    public static void main(String... args)
    {
        int length = 190;
        CL.setExceptionsEnabled(true);
       
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(ScanTest.class, "Scan.cl"));
       
        CFloatBuffer floatBuffer = configuration.allocFloat("floatBuffer", length, READ_WRITE);      
        floatBuffer.mapWriteBuffer(configuration.queue(), buffer->
        {
            Random r = new Random();
            while(buffer.hasRemaining())
                buffer.put(r.ints(length, 0, 20).limit(1).findFirst().getAsInt());
            System.out.println(Arrays.toString(buffer.array()));
        });
        
        
        CFloatScan scan = new CFloatScan(configuration);
        scan.init(floatBuffer);
        scan.executeTotalElements();
        
         
        System.out.println(scan.getTotalElements());
    }
}
