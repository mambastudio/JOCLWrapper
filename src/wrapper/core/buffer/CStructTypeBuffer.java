/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.buffer;

import coordinate.struct.ByteStruct;
import coordinate.struct.StructByteArray;
import java.nio.ByteBuffer;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CMemory;
import wrapper.core.CallBackStructByteArray;

/**
 *
 * @author user
 * @param <B>
 */
public class CStructTypeBuffer <B extends ByteStruct> extends CMemory<ByteBuffer> {
    private final StructByteArray<B> structByteArray;
    
    public CStructTypeBuffer(cl_mem memory, StructByteArray<B> structByteArray, ByteBuffer buffer, Pointer pointer, long cl_size) {
        super(memory, buffer, pointer, cl_size);
        this.structByteArray = structByteArray;
    }
    
    public StructByteArray<B> mapReadBuffer(CCommandQueue queue, CallBackStructByteArray<B> function)
    {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        function.call(structByteArray);
        return structByteArray;
    }
    
    public StructByteArray<B>  mapWriteBuffer(CCommandQueue queue, CallBackStructByteArray<B> function)
    { 
        buffer.clear(); //reset buffer
        function.call(structByteArray);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        return structByteArray;
    }
    
    public StructByteArray<B> getStructByteArray()
    {
        return  structByteArray;
    }
    
    public B get(int i)
    {        
        return structByteArray.get(i);
    }
        
    public int getSize()
    {
        return structByteArray.size();
    }   
}
