/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistent;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.memory.values.IntValue;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class Persistent {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(Persistent.class, "Persistent.cl"));
        
        int length = 32; //any length is allowed
        int[] data = new Random().ints(length, 0, 1).toArray(); //random [1, 1, 0, 0, 1, 1, 1, 1, 1, 0]
    
        int LOCALSIZE   = 8; //should be a pow of 2, read above why we use the value of 128
        int GLOBALSIZE  = 32; //should be a power of 2 based on local size
        
        
        CMemory<IntValue> a     = configuration.createFromI(IntValue.class, data, READ_WRITE);
        CMemory<IntValue> b     = configuration.createBufferI(IntValue.class, length, READ_WRITE);
        CMemory<IntValue> ahead = configuration.createFromI(IntValue.class, new int[]{0}, READ_WRITE);
        CMemory<IntValue> bhead = configuration.createFromI(IntValue.class, new int[]{0}, READ_WRITE);
        CMemory<IntValue> count = configuration.createFromI(IntValue.class, new int[]{32}, READ_WRITE);
        
        CKernel persistentKernel = configuration.createKernel("persistent", ahead, bhead, count, a, b);
        
        configuration.execute1DKernel(persistentKernel, 32, 8);
        
        b.transferFromDevice();        
        int[] outputB = (int[]) b.getBufferArray();
        System.out.println(Arrays.toString(outputB));
        
    }
}
