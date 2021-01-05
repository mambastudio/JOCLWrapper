/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compact;

import java.util.Arrays;
import java.util.Random;

/**
 * 
 * @author user
 * 
 * http://www.davidespataro.it/cuda-stream-compaction-efficient-implementation/
 */
public class CPUCompact {
    static int GLOBALSIZE = 12;
    static int LOCALSIZE = 4; //seems this the most optimal ~ equivalent to a 32 bit integer
    
    public static void main(String... args)
    {
        int[] procCount  = new int[getWorkGroupSize()];
        int[] procOffset = new int[getWorkGroupSize()];
        
        int predicate[] = new int[GLOBALSIZE];
        int output[] = new int[GLOBALSIZE];
        int bitPredicate[] = new int[getWorkGroupSize()];
        
        Random r = new Random();
        for(int i = 0; i<predicate.length; i++)
            predicate[i] = r.ints(GLOBALSIZE, 0, 2).limit(1).findFirst().getAsInt();        
        System.out.println(Arrays.toString(predicate));
        
        //phase1        
        for(int workGroupID = 0; workGroupID<getWorkGroupSize(); workGroupID++)
        {           
            for(int localID=0;localID<LOCALSIZE;localID++)
            {
                int index = LOCALSIZE*workGroupID + localID;
                if(index < GLOBALSIZE)
                    if(predicate[index] > 0)
                        bitPredicate[workGroupID] |= (1 << localID);   //store bit like 10010011 
            }
            procCount[workGroupID] = Long.bitCount(bitPredicate[workGroupID]);
        }
        System.out.println(Arrays.toString(procCount));
        
        //phase 2
        System.arraycopy(procCount, 0, procOffset, 1, getWorkGroupSize() - 1);       
        Arrays.parallelPrefix(procOffset, (x, y) -> (x + y));
        System.out.println(Arrays.toString(procOffset));
        
        //phase3 - efficient stream compaction on wide simd many-core architectures pg 5
        //amazing algorithm
        for(int workGroupID = 0; workGroupID < getWorkGroupSize(); workGroupID++)
        {         
            //local barrier
            for(int localID=0;localID<LOCALSIZE;localID++)
            {
                int index = LOCALSIZE*workGroupID + localID;
                if(index < GLOBALSIZE)
                    if(predicate[index] > 0)
                    {
                        int m1 = bitPredicate[workGroupID] & ((1 << localID) - 1);
                        int s1  = Integer.bitCount(m1);                        
                        output[(int)(procOffset[workGroupID] + s1)] = predicate[index];                       
                    }
            }            
        }
        System.out.println(Arrays.toString(output));
    }
    
    public static int getWorkGroupSize()
    {
        int a = GLOBALSIZE/LOCALSIZE;
        int b = GLOBALSIZE%LOCALSIZE; //has remainder
        
        return (b > 0)? a + 1 : a;
            
    }
}
