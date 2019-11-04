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
public class RadixSort {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLFileReader.readFile(RadixSort.class, "RadixSortUInt.cl"));
        
        int[] randomValuesArray = new int[]{4, 2, 9, 1, 1, 0, 5};//new Random().ints(5, 0, 5).toArray();        
        int globalSize = randomValuesArray.length;
        
        CIntBuffer values        = configuration.createFromIntArray("values", READ_WRITE, randomValuesArray);
        CIntBuffer bit           = configuration.allocIntValue("bit", 0, READ_WRITE);
        CIntBuffer values0       = configuration.allocInt("values0", globalSize, READ_WRITE);
        CIntBuffer values1       = configuration.allocInt("values1", globalSize, READ_WRITE);
        CIntBuffer insertIndex0  = configuration.allocIntValue("insertIndex0", 0, READ_WRITE);
        CIntBuffer insertIndex1  = configuration.allocIntValue("insertIndex1", 0, READ_WRITE);
        
        CKernel sortBitKernel    = configuration.createKernel("sortBit", values, bit, values0, values1, insertIndex0, insertIndex1); 
        CKernel insertBitKernel  = configuration.createKernel("insertBit", values, bit, values0, values1, insertIndex0, insertIndex1);
        
        //System.out.println(Arrays.toString(randomValuesArray));
        System.out.println(Arrays.toString(randomValuesArray));
        for (int bbit = 0; bbit < 31; ++bbit)
        {
            configuration.setIntValue(0, insertIndex0);
            configuration.setIntValue(0, insertIndex1);
            configuration.setIntValue(bbit, bit);
            
            configuration.executeKernel1D(sortBitKernel, globalSize, 1); 
            configuration.executeKernel1D(insertBitKernel, globalSize, 1); 
            
            //System.out.println(configuration.getIntValueFromDevice(insertIndex1));
        }
        configuration.readFromDevice(values);
        
        System.out.println(Arrays.toString(randomValuesArray));
    }
}
