/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.buffer;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CBufferMemory;
import wrapper.core.CCommandQueue;

/**
 *
 * @author user
 */
public class CFloatBuffer extends CBufferMemory<FloatBuffer>
{    
    public CFloatBuffer(cl_mem mem, FloatBuffer buffer, Pointer pointer, long clSize)
    {   
        super(mem, buffer, pointer, clSize);        
    }
            
    public float get(int i)
    {        
        return buffer.get(i);
    }
    
    public float get()
    {
        return buffer.get();
    }

    public void put(float... src)
    {
        buffer.put(src);
    }
    
    public int remaining()
    {
        return buffer.remaining();
    }
    
    public void rewind()
    {
        buffer.rewind();
    }
    
    public void mapBufferToDevice(CCommandQueue queue)
    {
        this.mapWriteBuffer(queue, buf -> {});
    }
    
    public void transferFromBufferToDevice(CCommandQueue queue)
    {
        this.mapWriteBuffer(queue, buf -> {});
    }
    
    public void transferFromDeviceToBuffer(CCommandQueue queue)
    {
        this.mapReadBuffer(queue, buf -> {});
    }
    
    public void mapWriteValue(CCommandQueue queue, float value)
    {
        this.mapWriteBuffer(queue, buf-> {buf.put(value);});        
    }
    
    public float mapReadValue(CCommandQueue queue)
    {        
        FloatBuffer floatBuffer = this.mapReadBuffer(queue, buf -> {});
        return floatBuffer.get(0);
    }
    
    public void setArray(CCommandQueue queue, float... array)
    {
        if(array.length != this.buffer.capacity())
            return;
        this.mapWriteBuffer(queue, buf -> buf.put(array));
    }
    
    public float[] getArray(CCommandQueue queue)
    {
        this.mapReadBuffer(queue, buf -> {});
        return this.buffer.array();
    }
        
    @Override
    public String toString()
    {
        return Arrays.toString(buffer.array());
    }    
}
