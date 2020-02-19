/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 */
public class HybridCompact {
    static int GLOBALSIZE = 0;
    static int LOCALSIZE = 31;
    static int length = 32;
    public static void main(String... args)
    {
        CL.setExceptionsEnabled(true);
        GLOBALSIZE = getGlobalSize(); 
        OpenCLPlatform platform     = OpenCLPlatform.getDefault(CLFileReader.readFile(HybridCompact.class, "Compact.cl"));
        CIntBuffer cpredicate       = platform.allocInt("predicate", length, READ_WRITE);
        CIntBuffer cbitpredicate    = platform.allocInt("bitpredicate", getNumOfGroups(), READ_WRITE);
        CIntBuffer coutput          = platform.allocInt("output", length, READ_WRITE);
        CIntBuffer cproccount       = platform.allocInt("proccount", getNumOfGroups(), READ_WRITE);
        CIntBuffer cprocoffset      = platform.allocInt("procoffset", getNumOfGroups(), READ_WRITE);        
        CIntBuffer cactuallength    = platform.allocIntValue("actualsize", length, READ_WRITE);
        
        //
        CKernel bitPredicateKernel  = platform.createKernel("BitPredicate", cpredicate, cbitpredicate, cactuallength);
        CKernel countKernel         = platform.createKernel("Count", cbitpredicate, cproccount);
        CKernel compactSIMDKernel   = platform.createKernel("CompactSIMD", cpredicate, cbitpredicate, cprocoffset, coutput, cactuallength);
        
        Random r = new Random();
        cpredicate.mapWriteBuffer(platform.queue(), buffer->{
            int[] predicate = buffer.array();
            for(int i = 0; i<predicate.length; i++)
                predicate[i] = (r.ints(length, 0, 4).limit(1).findFirst().getAsInt());        
            System.out.println(Arrays.toString(predicate));
        });
        
        
        //phase 1
        platform.executeKernel1D(bitPredicateKernel, GLOBALSIZE, LOCALSIZE);        
        platform.executeKernel1D(countKernel, getNumOfGroups(), 1);
        
        cproccount.mapReadBuffer(platform.queue(), buffer->{
            int[] procCount = buffer.array();
            System.out.println(Arrays.toString(procCount));
        });
        
        
        //phase 2
         cprocoffset.mapWriteBuffer(platform.queue(), buffer->{
            int[] procC = cproccount.getArray(platform.queue());
            int[] procO = buffer.array();
            //exclusive scan
            System.arraycopy(procC, 0, procO, 1, getNumOfGroups() - 1); 
            Arrays.parallelPrefix(procO, (x, y) -> (x + y));
            int lastIndex = procC.length-1;            
            System.out.println(Arrays.toString(procO));
        });
         
         
        //phase 3
        platform.executeKernel1D(compactSIMDKernel, GLOBALSIZE, LOCALSIZE);        
        coutput.mapReadBuffer(platform.queue(), buffer->{
            int[] output = buffer.array();
            System.out.println(Arrays.toString(output));
        });
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
