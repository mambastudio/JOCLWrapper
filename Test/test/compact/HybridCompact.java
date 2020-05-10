/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.    
 *
 */
package test.compact;

import java.util.Arrays;
import java.util.Random;
import org.jocl.CL;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLPlatform;
import wrapper.core.buffer.CIntBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 *
 * based on two papers
 * [main paper]: Efficient Stream Compaction on Wide SIMD Many-Core Architectures
 *
 * [supplement idea paper]: Static GPU threads and an improved scan algorithm
 */
public class HybridCompact {
    static int GLOBALSIZE = 0;
    static int LOCALSIZE = 31;
    static int length = 10;
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        GLOBALSIZE = getGlobalSize(); 
        OpenCLPlatform platform     = OpenCLPlatform.getDefault(CLFileReader.readFile(HybridCompact.class, "Compact.cl"));
        CIntBuffer cpredicate       = platform.allocInt("predicate", length, READ_WRITE);
        CIntBuffer cbitpredicate    = platform.allocInt("bitpredicate", getNumOfGroups(), READ_WRITE);
        CIntBuffer cinput           = platform.allocInt("input", length, READ_WRITE);
        CIntBuffer coutput          = platform.allocInt("output", length, READ_WRITE);
        CIntBuffer cproccount       = platform.allocInt("proccount", getNumOfGroups(), READ_WRITE);
        CIntBuffer cprocoffset      = platform.allocInt("procoffset", getNumOfGroups(), READ_WRITE);        
        CIntBuffer cactuallength    = platform.allocIntValue("actualsize", length, READ_WRITE);
        CIntBuffer cgroupsize       = platform.allocIntValue("cgroupsize", getNumOfGroups(), READ_WRITE);
        CIntBuffer ccompactlength   = platform.allocIntValue("compactlength", 0, READ_WRITE);
        
        //
        CKernel bitPredicateKernel          = platform.createKernel("BitPredicate", cpredicate, cbitpredicate, cactuallength);
        CKernel countKernel                 = platform.createKernel("Count", cbitpredicate, cproccount);
        CKernel sequentialPrefixSumKernel   = platform.createKernel("SequentialPrefixSum", cbitpredicate, cproccount, cprocoffset, cgroupsize, ccompactlength);
        CKernel compactSIMDKernel           = platform.createKernel("CompactSIMD", cinput, cpredicate, cbitpredicate, cprocoffset, coutput, cactuallength);
        CKernel setPredicateKernel          = platform.createKernel("SetPredicate", cinput, cpredicate, cactuallength);
        
        Random r = new Random();
        cinput.mapWriteBuffer(platform.queue(), buffer->{            
            int[] input = buffer.array();
            for(int i = 0; i<input.length; i++)
            {
                int a = (r.ints(1, 0, 2).limit(1).findFirst().getAsInt());
                int b = (r.ints(1, 1, 10).limit(1).findFirst().getAsInt());
                input[i] = a * b;
            }
            System.out.println(Arrays.toString(input));
        });
        
        //phase 1
        platform.executeKernel1D(setPredicateKernel, GLOBALSIZE, 1);
        platform.executeKernel1D(bitPredicateKernel, GLOBALSIZE, LOCALSIZE);        
        platform.executeKernel1D(countKernel, getNumOfGroups(), 1);        
        long time1 = System.nanoTime();
        //phase 2
        platform.executeKernel1D(sequentialPrefixSumKernel, 1, 1);              
        //phase 3
        platform.executeKernel1D(compactSIMDKernel, GLOBALSIZE, LOCALSIZE); 
        long time2 = System.nanoTime();
        
        coutput.mapReadBuffer(platform.queue(), buffer->{
            System.out.println(Arrays.toString(buffer.array()));
        });
        System.out.println(ccompactlength.mapReadValue(platform.queue()));
                
        double mTime = (double)(time2 - time1)/1_000_000_000;
        System.out.printf("%.12f \n", mTime);
    }
    
    
    public static int getNumOfGroups()
    {
        int a = length/LOCALSIZE;
        int b = length%LOCALSIZE; //has remainder
        
        return (b > 0)? a + 1 : a;
            
    }
    
    public static int getGlobalSize()
    {
        return getNumOfGroups() * LOCALSIZE; 
    }
}
