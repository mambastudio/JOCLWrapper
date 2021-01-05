/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.algorithms;

import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.memory.values.FloatValue;
import wrapper.core.memory.values.IntValue;

/*
What to put in kernel

int getInteger(int index, int length, global int* data)
{
    if(index >= length)
        return INT_MAX;
    else 
        return data[index];
}

bool isGreaterThanInteger(int posStart, int posEnd, int length, global int* data)
{
    int value1 = getInteger(posStart, length, data);
    int value2 = getInteger(posEnd, length, data);

    return value1 > value2;
}

void swapInteger(int PosSIndex, int PosEIndex, global int* data)
{       
    int tmp          = data[PosSIndex];
    data[PosSIndex]  = data[PosEIndex];
    data[PosEIndex]  = tmp;
}

__kernel void butterfly1Integer(global int* data, global int* lengthSize, global float* powerX)
{
     int gid = get_global_id(0);

     int t = gid;
     int radix = 2;
     int length = lengthSize[0];
     int PowerX = powerX[0];

     int yIndex      = (int) (t/(PowerX/radix));
     int kIndex      = (int) (t%(PowerX/radix));
     int PosStart    = (int) (kIndex + yIndex * PowerX);
     int PosEnd      = (int) (PowerX - kIndex - 1 + yIndex * PowerX);

     if(isGreaterThanInteger(PosStart, PosEnd, length, data))
         swapInteger(PosStart, PosEnd, data);
}

__kernel void butterfly2Integer(global int* data, global int* lengthSize, global float* powerX)
{
    int gid = get_global_id(0);

    int t = gid;
    int radix = 2;
    int length = lengthSize[0];
    int PowerX = powerX[0];

    int yIndex      = (int) (t/(PowerX/radix));
    int kIndex      = (int) (t%(PowerX/radix));
    int PosStart    = (int) (kIndex + yIndex * PowerX);
    int PosEnd      = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                            
    if(isGreaterThanInteger(PosStart, PosEnd, length, data))
        swapInteger(PosStart, PosEnd, data);


}
*/

/**
 *
 * @author user
 * @param <DataType>
 */

public class ButterflySort<DataType>
{
    private OpenCLConfiguration configuration = null;
    
    private final CMemory cdata;
    private final CMemory<IntValue> clength;
    private final CMemory<FloatValue> cpowerx;
    
    private final Class<DataType> clazz;
    
    public ButterflySort(OpenCLConfiguration configuration, Class<DataType> clazz, CMemory data, int length)
    {
        this.configuration = configuration;
        this.cdata = data;
        this.clazz = clazz;
        
        clength = configuration.createValueI(IntValue.class, new IntValue(length), READ_ONLY);
        cpowerx = configuration.createValueF(FloatValue.class, new FloatValue(0), READ_WRITE);        
    }
    
    public void sort()
    {
        //start of sort structure
        int radix  = 2;        
        int until = until(clength.getCL().v); 
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;
                
        int globalSize = T;
        int localSize = globalSize<256 ? globalSize : 256;
        
        //kernel initialization
        CKernel cbutterfly1    = configuration.createKernel("butterfly1"+clazz.getSimpleName(), cdata, clength, cpowerx);
        CKernel cbutterfly2    = configuration.createKernel("butterfly2"+clazz.getSimpleName(), cdata, clength, cpowerx);
    
        for(int xout = 1; xout<=until; xout++)
        {     
            cpowerx.setCL(new FloatValue((float)Math.pow(radix, xout))); //PowerX = (Math.pow(radix, xout));      
                                    
            // OpenCL kernel call
            configuration.execute1DKernel(cbutterfly1, globalSize, localSize); 
            
            if(xout > 1)
            {                
                for(int xin = xout; xin > 0; xin--)
                {
                    cpowerx.setCL(new FloatValue((float)Math.pow(radix, xin))); //PowerX = (Math.pow(radix, xin));
                    
                    // OpenCL kernel call
                    configuration.execute1DKernel(cbutterfly2, globalSize, localSize); 
                    
                }
            }
        }        
    }
    
    public void sortThenDeviceToBuffer()
    {
        sort();
        cdata.transferFromDevice();
    }
    
    private int log2( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }
    
    private int until(int length)
    {
        int log2 = log2(length);
        int difference = (int)(Math.pow(2, log2)) - length;
        
        if(difference == 0) return log2;
        else                return log2+1;
    }
}
