/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import static org.jocl.CL.CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.algorithms.CPrefixSum;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestPrefixSum {
    public static void main(String... args)
    {  
        CL.setExceptionsEnabled(true);       
        OpenCLPlatform configuration = OpenCLPlatform.getDefault(CLSource.readFiles());
        
        int length                  = 1000;                         
        CFloatBuffer floatBuffer    = configuration.allocFloat("floatBuffer", length, READ_WRITE);
        CIntBuffer total            = configuration.allocIntValue("total", 0, READ_WRITE);
        
        CPrefixSum prefixSum        = new CPrefixSum(configuration); 
        prefixSum.init(length, total);
        
        floatBuffer.mapWriteBuffer(configuration.queue(), buffer->
        {
            Random r = new Random();
            while(buffer.hasRemaining())
                buffer.put(r.ints(length, 0, 2).limit(1).findFirst().getAsInt());
            System.out.println(Arrays.toString(buffer.array()));
        });
               
        CKernel generateFloatPredicateKernel = configuration.createKernel("predicateFloat", floatBuffer, prefixSum.getPredicate());
        
        long[] value = new long[1];
        CL.clGetKernelWorkGroupInfo(generateFloatPredicateKernel.getId(), configuration.device().getId(), CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE, Sizeof.cl_long, Pointer.to(value), null);
        System.out.println(Arrays.toString(value));
        
        prefixSum.clearPredicate();
        configuration.executeKernel1D(generateFloatPredicateKernel, length, 1);
        prefixSum.execute();
        
        prefixSum.printlnPredicate();
        prefixSum.printlnPrefixSum();
        prefixSum.printlnTotalCount();
        
        int[] prefix = prefixSum.getPredicate().getArray(configuration.queue());
        System.out.println(Arrays.toString(prefix));
        Arrays.parallelPrefix(prefix, (x, y) -> x + y);
        System.out.println(Arrays.toString(prefix));
    }
}
