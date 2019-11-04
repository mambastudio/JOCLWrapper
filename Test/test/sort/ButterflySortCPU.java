/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.sort;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author user
 */
public class ButterflySortCPU {
    public static void main(String... args)
    {
        int[] data = new Random().ints(16, 0, 90).toArray();//{7, 3, 2, 5, 6, 4, 0, 1};//new Random().ints(4, 0, 8).toArray();//{15, 33, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};//{7, 3, 2, 5, 6, 4, 0, 1}; //new Random().ints(32, 0, 90).toArray();
            
        System.out.println(Arrays.toString(data));
        butterflySort(data);
        System.out.println(Arrays.toString(data));
    }
    
    public static void minmaxButterfly(int[] data)
    {       
        int radix  = 2;
        int T = data.length/radix;
        
        for(int stage = 1; stage<=log2(data.length); stage++)
        {            
            double PowerX = (Math.pow(radix, stage));
            
            // parallel
            for(int t = 0; t<T; t++)
            {                
                int yIndex      = (int) (t/(PowerX/radix));  
                int kIndex      = (int) (t%(PowerX/radix));
                int PosSIndex   = (int) (kIndex + yIndex * PowerX);
                int PosEIndex   = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                
                if(data[PosSIndex] > data[PosEIndex])
                   swap(PosSIndex, PosEIndex, data);
            }
        }
    }
    
    public static void butterflySort(int[] data)
    {
        int radix  = 2;        
        int until = until(data);
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;
        double PowerX = 0;
        
        for(int xout = 1; xout<=until; xout++)
        {            
            PowerX = (Math.pow(radix, xout));
            
            // parallel
            for(int t = 0; t<T; t++)
            {                
                int yIndex      = (int) (t/(PowerX/radix));  
                int kIndex      = (int) (t%(PowerX/radix));
                int PosStart    = (int) (kIndex + yIndex * PowerX);
                int PosEnd      = (int) (PowerX - kIndex - 1 + yIndex * PowerX);
                               
                if(isGreaterThan(PosStart, PosEnd, data))
                   swap(PosStart, PosEnd, data);
            }
            
            if(xout > 1)
            {                
                for(int xin = xout; xin > 0; xin--)
                {
                    PowerX = (Math.pow(radix, xin));
                    
                    // parallel
                    for(int t = 0; t<T; t++)
                    { 
                        int yIndex      = (int) (t/(PowerX/radix));  
                        int kIndex      = (int) (t%(PowerX/radix));
                        int PosStart    = (int) (kIndex + yIndex * PowerX);
                        int PosEnd      = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                                                
                        if(isGreaterThan(PosStart, PosEnd, data))
                            swap(PosStart, PosEnd, data);
                    }
                }
            }
        }
    }    
        
    static void swap(int PosSIndex, int PosEIndex, int... data)
    {       
        int tmp          = data[PosSIndex];
        data[PosSIndex]  = data[PosEIndex];
        data[PosEIndex]  = tmp;               
    }
    
    //https://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers    
    public static int log2( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }
    
    public static int until(int[] data)
    {
        int log2 = log2(data.length);
        int difference = (int)(Math.pow(2, log2)) - data.length;
        
        if(difference == 0) return log2;
        else                return log2+1;
    }
            
    public static int get(int index, int[] data)
    {
        if(index >= data.length)
            return Integer.MAX_VALUE;
        else 
            return data[index];
    }
    
    public static boolean isGreaterThan(int posStart, int posEnd, int[] data)
    {
        int value1 = get(posStart, data);
        int value2 = get(posEnd, data);
        
        return value1 > value2;
    }
}
