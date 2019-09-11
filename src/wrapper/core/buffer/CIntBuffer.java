/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.buffer;

import java.nio.IntBuffer;
import java.util.Arrays;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CBufferMemory;
import wrapper.core.CCommandQueue;

/**
 *
 * @author user
 */
public class CIntBuffer extends CBufferMemory<IntBuffer>
{
    public CIntBuffer(cl_mem mem, IntBuffer buffer, Pointer pointer, long clSize)
    {   
        super(mem, buffer, pointer, clSize);
    }
            
    public int get(int i)
    {
        return buffer.get(i);
    }
    
    public int get()
    {
        return buffer.get();
    }
    
    public void transferFromBufferToDevice(CCommandQueue queue)
    {
        this.mapWriteBuffer(queue, buf -> {});
    }
    
    public void transferFromDeviceToBuffer(CCommandQueue queue)
    {
        this.mapReadBuffer(queue, buf -> {});
    }
    
    public void mapWriteValue(CCommandQueue queue, int value)
    {
        this.mapWriteBuffer(queue, buf-> {buf.put(value);});        
    }
    
    public int mapReadValue(CCommandQueue queue)
    {        
        IntBuffer floatBuffer = this.mapReadBuffer(queue, buf -> {});
        return floatBuffer.get(0);
    }
    
    public void setArray(CCommandQueue queue, int... array)
    {
        if(array.length != this.buffer.capacity())
            return;
        this.mapWriteBuffer(queue, buf -> buf.put(array));
    }
    
    public int[] getArray(CCommandQueue queue)
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
