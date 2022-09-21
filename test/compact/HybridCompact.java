/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.    
 *
 */
package compact;

import java.util.Random;
import org.jocl.CL;
import wrapper.core.memory.values.IntValue;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
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
    static int length = 80;
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        GLOBALSIZE = getGlobalSize(); 
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(HybridCompact.class, "Compact.cl"));
       
        CMemory<IntValue> cpredicate       = configuration.createBufferI(IntValue.class, length, READ_WRITE);
        CMemory<IntValue> cbitpredicate    = configuration.createBufferI(IntValue.class, getNumOfGroups(), READ_WRITE);
        CMemory<IntValue> cinput           = configuration.createBufferI(IntValue.class, length, READ_WRITE);
        CMemory<IntValue> coutput          = configuration.createBufferI(IntValue.class, length, READ_WRITE);
        CMemory<IntValue> cproccount       = configuration.createBufferI(IntValue.class, getNumOfGroups(), READ_WRITE);
        CMemory<IntValue> cprocoffset      = configuration.createBufferI(IntValue.class, getNumOfGroups(), READ_WRITE);        
        CMemory<IntValue> cactuallength    = configuration.createValueI(IntValue.class, new IntValue(length), READ_WRITE);
        CMemory<IntValue> cgroupsize       = configuration.createValueI(IntValue.class, new IntValue(getNumOfGroups()), READ_WRITE);
        CMemory<IntValue> ccompactlength   = configuration.createValueI(IntValue.class, new IntValue(0), READ_WRITE);
        
        //
        CKernel bitPredicateKernel          = configuration.createKernel("BitPredicate", cpredicate, cbitpredicate, cactuallength);
        CKernel countKernel                 = configuration.createKernel("Count", cbitpredicate, cproccount);
        CKernel sequentialPrefixSumKernel   = configuration.createKernel("SequentialPrefixSum", cbitpredicate, cproccount, cprocoffset, cgroupsize, ccompactlength);
        CKernel compactSIMDKernel           = configuration.createKernel("CompactSIMD", cinput, cpredicate, cbitpredicate, cprocoffset, coutput, cactuallength);
        CKernel setPredicateKernel          = configuration.createKernel("SetPredicate", cinput, cpredicate, cactuallength);
        
        Random r = new Random();
        cinput.loopWrite((value, index)->{ 
            int a = (r.ints(1, 0, 2).limit(1).findFirst().getAsInt());
            int b = (r.ints(1, 1, 10).limit(1).findFirst().getAsInt());
            value.set(a * b);
            System.out.print(value.v +" ");         
        }); System.out.println();
        
        
        //phase 1
        configuration.execute1DKernel(setPredicateKernel, GLOBALSIZE, 1);
        configuration.execute1DKernel(setPredicateKernel, GLOBALSIZE, 1);
        configuration.execute1DKernel(bitPredicateKernel, GLOBALSIZE, LOCALSIZE);        
        configuration.execute1DKernel(countKernel, getNumOfGroups(), 1);        
        long time1 = System.nanoTime();
        //phase 2
        configuration.execute1DKernel(sequentialPrefixSumKernel, 1, 1);              
        //phase 3
        configuration.execute1DKernel(compactSIMDKernel, GLOBALSIZE, LOCALSIZE); 
        long time2 = System.nanoTime();
        
        coutput.loopRead((value, i)->{
            System.out.print(value.v +" ");
        });
        
        System.out.println(ccompactlength.getCL().v);
                
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
