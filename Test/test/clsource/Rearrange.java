/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.clsource;

import java.util.Arrays;
import org.jocl.CL;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class Rearrange {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(RadixSort.class, "Rearrange.cl"));
        
        int[] randomValuesArray = new int[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0};//new Random().ints(5, 0, 5).toArray();        
        int globalSize = randomValuesArray.length;
        
        
        CIntBuffer values        = configuration.createFromIntArray("values", READ_WRITE, randomValuesArray);        
        CIntBuffer values0       = configuration.allocInt("values0", globalSize, READ_WRITE);
        CIntBuffer values1       = configuration.allocInt("values1", globalSize, READ_WRITE);
        CIntBuffer insertIndex0  = configuration.allocIntValue("insertIndex0", 0, READ_WRITE);
        CIntBuffer insertIndex1  = configuration.allocIntValue("insertIndex1", 0, READ_WRITE); 
        
        CKernel sortBitKernel    = configuration.createKernel("sortbit", values, values0, values1, insertIndex0, insertIndex1); 
        CKernel insertBitKernel  = configuration.createKernel("concatenate", values, values0, values1, insertIndex0, insertIndex1);
        
        System.out.println(Arrays.toString(randomValuesArray));
        
        configuration.executeKernel1D(sortBitKernel, globalSize, 1); 
        configuration.executeKernel1D(insertBitKernel, globalSize, 1); 
        
        configuration.readFromDevice(values);
        
        System.out.println(Arrays.toString(randomValuesArray));
       
    }
}
