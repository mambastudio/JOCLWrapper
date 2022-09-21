/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prefixsum;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import static wrapper.core.memory.CLocalMemory.LOCALINT;
import wrapper.core.memory.values.IntValue;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 * 
 * provides a proof of concept of undertaking a prefix sum using opencl (Jocl)
 * 
 */
public class PrefixSumTest {
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(PrefixSumTest.class, "PrefixSum.cl"));
    
        
        
        int length = 333; //any length is allowed
        int[] data = new Random().ints(length, 0, 2).toArray(); //random [1, 1, 0, 0, 1, 1, 1, 1, 1, 0]
        
        /**
         * Local size is hardware specific if you're using memory fence or local barriers.
         * 
         * For example, AMD, INTEL GPU it goes to 128, beyond it, it produces wrong results or crushes
         * Probably due to their SIMD width being a multiple of 64
         * NVIDIA might provide different results since it has a SIMD width of 32 if local size extends beyond
         * certain value like 128.
         * 
         * Note that GPU local size of 128 does not work in CPU due to also for memory fence or local barriers. 
         * Most CPU have a SIMD width until 16 depending on the hardware you use          
         * 
         * NB:  This only applies when you use memory fence or local barriers in your cl code
         *      If not (when you implement other algorithms), then LOCALSIZE has no issue (until hardware permitted such as 256 GPU or 512 for CPU)
         * 
         * In short, it seems memory fence or local barriers are quite delicate/sensitive on hardware type
         * and one should be careful on that.
         * 
        */        
        int LOCALSIZE   = 128; //should be a pow of 2, read above why we use the value of 128
        int GLOBALSIZE  = getGlobal(length, LOCALSIZE); //should be a power of 2 based on local size
        int GROUPSIZE   = getNumOfGroups(GLOBALSIZE, LOCALSIZE);
        
        System.out.println(Arrays.toString(data));
        
        CMemory<IntValue> clength           = configuration.createValueI(IntValue.class, new IntValue(length), READ_WRITE);
        CMemory<IntValue> cpredicate        = configuration.createFromI(IntValue.class, data, READ_WRITE);
        CMemory<IntValue> cprefixsum        = configuration.createBufferI(IntValue.class, length, READ_WRITE);
        
        CMemory<IntValue> cgroupSum         = configuration.createBufferI(IntValue.class, GROUPSIZE, READ_WRITE);
        CMemory<IntValue> cgroupPrefixSum   = configuration.createBufferI(IntValue.class, GROUPSIZE, READ_WRITE);
        CMemory<IntValue> cgroupSize        = configuration.createValueI(IntValue.class, new IntValue(GROUPSIZE), READ_WRITE);
        
        CKernel localScanIntegerKernel      = configuration.createKernel("localScanInteger", cpredicate, cprefixsum, cgroupSum, clength, LOCALINT);
        CKernel groupScanIntegerKernel      = configuration.createKernel("groupScanInteger", cgroupSum, cgroupPrefixSum, cgroupSize);
        CKernel globalScanIntegerKernel     = configuration.createKernel("globalScanInteger", cprefixsum, cgroupPrefixSum, clength);
        
        //these three kernels would provide a prefix sum that is quite fast for a huge array
        configuration.execute1DKernel(localScanIntegerKernel, GLOBALSIZE, LOCALSIZE); //start doing a local (workgroup) size scan
        configuration.execute1DKernel(groupScanIntegerKernel, 1, 1); //Do group level scan. Quite fast. Don't underestimate opencl loops
        configuration.execute1DKernel(globalScanIntegerKernel, GLOBALSIZE, LOCALSIZE); //add the group level scan to the local size scan
        
        cprefixsum.transferFromDevice();        
        int[] output = (int[]) cprefixsum.getBufferArray();
        
        cgroupPrefixSum.transferFromDevice();
        int[] groupsum = (int[]) cgroupPrefixSum.getBufferArray();
        
        System.out.println(Arrays.toString(output));
        System.out.println(Arrays.toString(groupsum));
    }
    
    //returns a global size of power of 2
    public static int getGlobal(int size, int LOCALSIZE)
    {
        if (size % LOCALSIZE == 0) { 
            return (int) ((Math.floor(size / LOCALSIZE)) * LOCALSIZE); 
        } else { 
            return (int) ((Math.floor(size / LOCALSIZE)) * LOCALSIZE) + LOCALSIZE; 
        } 
    }
    
    //just the size of group size, but is depended on a local size power of 2
    public static int getNumOfGroups(int length, int LOCALSIZE)
    {
        int a = length/LOCALSIZE;
        int b = length%LOCALSIZE; //has remainder
        
        return (b > 0)? a + 1 : a;
            
    }
}
