/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.sort;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class ButterflySortGPU {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(ButterflySortGPU.class, "ButterflySort.cl"));
        
        int data[] = new Random().ints(1000, 0, 50).toArray();  //size, range (i, j)
        System.out.println(Arrays.toString(data));
                        
        CIntBuffer cdata   = configuration.createFromIntArray("data", READ_WRITE, data); 
        CIntBuffer clength = configuration.allocIntValue("lengthSize", data.length, READ_ONLY);
        CFloatBuffer cpowerx = configuration.allocFloatValue("powerX", 0, READ_WRITE);
        
        //start of sort structure
        int radix  = 2;        
        int until = until(data);
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;
        
        /*
            globalSize = "size/2"
            localSize  = "globalSize = 8   if globalSize < 256 "
                         "     256          otherwise          "           
        */                 
        int globalSize = T;
        int localSize = 1;
        
        //kernel initialization
        CKernel cbutterfly1    = configuration.createKernel("butterfly1", cdata, clength, cpowerx);
        CKernel cbutterfly2    = configuration.createKernel("butterfly2", cdata, clength, cpowerx);
        
        for(int xout = 1; xout<=until; xout++)
        {     
            configuration.setFloatValue((float)Math.pow(radix, xout), cpowerx); //PowerX = (Math.pow(radix, xout));      
                                    
            // OpenCL kernel call
            configuration.executeKernel1D(cbutterfly1, globalSize, localSize); 
            
            if(xout > 1)
            {                
                for(int xin = xout; xin > 0; xin--)
                {
                    configuration.setFloatValue((float)Math.pow(radix, xin), cpowerx); //PowerX = (Math.pow(radix, xin));
                    
                    // OpenCL kernel call
                    configuration.executeKernel1D(cbutterfly2, globalSize, localSize); 
                    
                }
            }
        }
        configuration.readFromDevice(cdata);
        System.out.println(Arrays.toString(cdata.getBuffer().array()));
        
        CResourceFactory.releaseAll();
    }
    
    public static int log2( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }
    
    public static int until(int[] data)
    {
        int log2 = log2(data.length);
        int difference = (int)(Math.pow(2, log2)) - data.length;
        
        if(difference == 0) return log2;
        else                return log2+1;
    }
}
