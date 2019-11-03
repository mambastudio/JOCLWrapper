/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import wrapper.core.buffer.local.CLocalBuffer;

/**
 *
 * @author user
 * @param <B>
 */
public class CBufferMemory<B extends Buffer> extends CMemory<B> 
{
    public static CLocalBuffer LOCALINT     =   new CLocalBuffer(Sizeof.cl_int);
    public static CLocalBuffer LOCALFLOAT   =   new CLocalBuffer(Sizeof.cl_float);    
    
    public CBufferMemory(cl_mem memory, B buffer, Pointer pointer, long cl_size)
    {
        super(memory, buffer, pointer, cl_size);
    }
    
    public B mapReadBuffer(CCommandQueue queue, CallBackFunction<B> function)
    {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        function.call(buffer);
        buffer.rewind();
        return buffer;
    }
    
    public B mapWriteBuffer(CCommandQueue queue, CallBackFunction<B> function)
    { 
        buffer.clear(); //reset buffer
        function.call(buffer);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        buffer.rewind();
        return buffer;
    }     
    
    public int getBufferSize()
    {
        return buffer.capacity();
    }
}
