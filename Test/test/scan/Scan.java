/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CBufferMemory;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 * @author user
 * 
 * Scan algorithm that is blelloch scan (exclusive)
 * 
 * Maximum implementation of scan of local size 256 is 16777216 (256^3)
 * - 16777216    (256^3)
 * - 65536       (256^2)
 * - 256         (256^1)
 * 
 * Maximum implementation of scan of local size 512 is 134217728 (512^3)
 * 
 * It has been modified to deal with arbitrary array size of "not n^2"
 */
public class Scan { 
    static int LOCALSIZECONSTANT = 512;
    
    public static void main(String... args)
    {
        int length = 30000;
        
        CL.setExceptionsEnabled(true);
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(Scan.class, "Scan.cl"));
        int array[] = new Random().ints(length, 0, 2).toArray();
        blelloch(configuration, array);
        
        System.out.println(length(length, 1));
        System.out.println(length(length, 2));
        System.out.println(length(length, 3));
                
    }
    
    public static void blelloch(OpenCLPlatform configuration, int[] array)
    {       
        System.out.println(Arrays.toString(array));
        
        int gSize1              = length(array.length, 1);
        int lSize1              = localsize(gSize1);                    
        int gSize2              = length(array.length, 2);
        int lSize2              = localsize(gSize2);
        int gSize3              = length(array.length, 3);
        int lSize3              = localsize(gSize3);
        
        CIntBuffer array1          = configuration.createFromIntArray("array1", READ_WRITE, array);        
        CIntBuffer array1length    = configuration.allocIntValue("array1length", array.length, READ_ONLY);
        CIntBuffer array2          = configuration.allocInt("array2", gSize2, READ_WRITE);
        CIntBuffer array2length    = configuration.allocIntValue("array2length", gSize2, READ_ONLY);
        CIntBuffer array3          = configuration.allocInt("array3", gSize3, READ_WRITE);
        CIntBuffer array3length    = configuration.allocIntValue("array2length", gSize3, READ_ONLY);
        
        CKernel scanKernel1    = configuration.createKernel("blelloch_scan_g", array1, array2, array1length, CBufferMemory.LOCALINT);  
        CKernel scanKernel2    = configuration.createKernel("blelloch_scan_g", array2, array3, array2length, CBufferMemory.LOCALINT);
        CKernel scanKernel3    = configuration.createKernel("blelloch_scan", array3, array3length, CBufferMemory.LOCALINT);        
        CKernel sumgKernel2    = configuration.createKernel("add_groups", array2, array3);
        CKernel sumgKernel1    = configuration.createKernel("add_groups_n", array1, array2, array1length);
        
        configuration.executeKernel1D(scanKernel1, gSize1, lSize1);           
        configuration.executeKernel1D(scanKernel2, gSize2, lSize2);       
        configuration.executeKernel1D(scanKernel3, gSize3, lSize3);       
        configuration.executeKernel1D(sumgKernel2, gSize2, lSize2);
        configuration.executeKernel1D(sumgKernel1, gSize1, lSize1);
        
        configuration.readFromDevice(array1);
        configuration.readFromDevice(array2);        
        System.out.println(Arrays.toString(array1.getBuffer().array()));
        
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
        
    public static int pow2length(int length)
    {
        int log2 = log2(length);
        int difference = (int)(Math.pow(2, log2)) - length;
        
        if(difference == 0) return length;
        else                return (int) Math.pow(2, log2+1);
    }
   
    public static int length(int size, int level)
    {
        int length = pow2length(size);
        
        length /= (int)Math.pow(LOCALSIZECONSTANT, level - 1);
        
        if(length == 0)
            return 1;
        int full_length = (int) Math.pow(LOCALSIZECONSTANT, log2(length));
        if(full_length == 0)
            return 1;
        else if(length > full_length)
            return full_length;
        else
            return length;
    }
    
    public static int localsize(int local)
    {
        return local > LOCALSIZECONSTANT ? LOCALSIZECONSTANT : local;
    }
}
