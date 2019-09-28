/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CBufferMemory;
import wrapper.core.CCommandQueue;

/**
 *
 * @author user
 */
public class CByteBuffer extends CBufferMemory<ByteBuffer>{
    public CByteBuffer(cl_mem mem, ByteBuffer buffer, Pointer pointer, long clSize)
    {   
        super(mem, buffer, pointer, clSize);
    }
    
    public byte[] getArray(CCommandQueue queue)
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
