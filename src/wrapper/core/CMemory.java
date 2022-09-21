/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.Struct;
import coordinate.unsafe.UnsafeUtils;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import static org.jocl.CL.CL_MEM_ALLOC_HOST_PTR;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_USE_HOST_PTR;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.clReleaseMemObject;
import org.jocl.Pointer;
import org.jocl.cl_mem;

/**
 *
 * @author user
 * @param <T>
 */
public abstract class CMemory<T extends Struct> extends CObject {
    
    protected final Buffer buffer;
    protected CCommandQueue queue;
    
    protected long cl_size;
    protected Pointer pointer;
    
    public CMemory(CCommandQueue queue, cl_mem memory, Class<T> clazz, Buffer buffer, Pointer pointer, long cl_size)
    {
        super(memory);
        this.buffer = buffer;
        this.queue = queue;
        this.pointer = pointer;
        this.cl_size = cl_size;
    }
    
    public void cleanByteBuffer()
    {
        UnsafeUtils.clean(buffer);       
    }
    
    public void mapReadMemory(CallBackFunction<CMemory<T>> function)
    {
        transferFromDevice();
        function.call(this);
    }
    
    public void mapWriteMemory(CallBackFunction<CMemory<T>> function)
    {
        function.call(this);
        transferToDevice();
    }
        
    public abstract void setCL(T t);
    public abstract T getCL();
    
    public abstract void set(int index, T t);
    public abstract T get(int index);
    
    public abstract void index(int index, CallBackFunction<T> function);
    
    public void transferFromDevice()
    {        
        if(buffer != null)
        {
            buffer.clear(); //reset buffer to 0 position
            buffer.rewind();
            queue.putReadBuffer(this);
            buffer.rewind(); //set read position to 0 but limit remain same 
        }
    }
    
    public void loopRead(BiConsumer<T, Integer> consume)
    {
        transferFromDevice();
        for(int i = 0; i<getSize(); i++)
        {
            consume.accept(get(i), i);
        }
    }
    
    public void transferToDevice()
    {        
        if(buffer != null)
        {
            buffer.clear(); //reset buffer       
            buffer.rewind();  // set read position to 0 but limit remains same
            queue.putWriteBuffer(this);
            buffer.rewind();
        }        
    }
    
    public void loopWrite(BiConsumer<T, Integer> consume)
    {        
        for(int i = 0; i<getSize(); i++)
        {
            consume.accept(get(i), i);
        }
        transferToDevice();
    }
    
    
    
    public Object getBufferArray()
    {
        if(buffer.hasArray())
            return buffer.array();
        return null;
    }
        
    public long getCLSize()
    {
        return cl_size;
    }
    
    public abstract long getSize();
    
    public boolean hasRemaining()
    {
        return buffer.hasRemaining();
    }
        
    public Pointer getPointer()
    {
        return pointer;
    }  
    
    public Buffer getBuffer()
    {
        return buffer;
    }
    
    public static void validateMemoryType(long flag)
    {
        if(!isValid(flag))
            throw new UnsupportedOperationException("flag not yet supported for now");            
    }
    
    public void release() 
    {
        clReleaseMemObject(getId());        
    }
    
    @Override
    public cl_mem getId()
    {
        return (cl_mem)super.getId();
    }
    
    
    public static boolean isValid(long flag)
    {
        return  flag == READ_ONLY || 
                flag == WRITE_ONLY || 
                flag == READ_WRITE || 
                flag == (READ_ONLY|COPY_HOST_PTR) || 
                flag == (WRITE_ONLY|COPY_HOST_PTR) || 
                flag == (READ_WRITE|COPY_HOST_PTR);  
    }
    
    public final static long READ_WRITE = CL_MEM_READ_WRITE;
    public final static long READ_ONLY = CL_MEM_READ_ONLY;
    public final static long WRITE_ONLY = CL_MEM_WRITE_ONLY;
    public final static long COPY_HOST_PTR = CL_MEM_COPY_HOST_PTR;
    public final static long USE_HOST_PTR = CL_MEM_USE_HOST_PTR;
    public final static long ALLOC_HOST_PTR = CL_MEM_ALLOC_HOST_PTR;
    
    public final static long READ_WRITE_COPY_PTR = READ_WRITE|COPY_HOST_PTR;
    public final static long READ_COPY_PTR = READ_ONLY|COPY_HOST_PTR;
    public final static long WRITE_COPY_PTR = WRITE_ONLY|COPY_HOST_PTR;
}
