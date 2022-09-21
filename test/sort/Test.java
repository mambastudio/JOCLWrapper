/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sort;

import java.util.Random;
import org.jocl.CL;
import wrapper.core.memory.values.IntValue;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class Test {
    public static void main(String... args)
    {
        
        CL.setExceptionsEnabled(true);
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(ButterflySortGPU.class, "ButterflySort.cl"));
             
        //init random values
        CMemory<IntValue> cdata   = configuration.createBufferI(IntValue.class, 115, READ_WRITE); 
        Random random = new Random();
        cdata.loopWrite((value, index) -> {
            value.set(random.nextInt(10));
            System.out.print(value.v + " ");
        });System.out.println();
        
        //sort
        ButterflySortGPU butterfly = new ButterflySortGPU(configuration, Integer.class, cdata);        
        butterfly.sortThenDeviceToBuffer();
        
        //read sorted values
        cdata.loopRead((value, index) -> {                             
            System.out.print(value.v + " ");            
        }); System.out.println();
    }
}
