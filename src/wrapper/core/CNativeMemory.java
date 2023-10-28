/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.MemoryStruct;
import java.util.function.Consumer;
import static org.jocl.CL.CL_MEM_ALLOC_HOST_PTR;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_USE_HOST_PTR;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import org.jocl.Pointer;
import org.jocl.cl_mem;

/**
 *
 * @author jmburu
 * @param <T>
 */
public class CNativeMemory<T extends MemoryStruct> extends CObject {
    
    protected final T array; 
    protected CCommandQueue queue;
    
    protected long cl_capacity;
    protected Pointer pointer;
    
    public CNativeMemory(CCommandQueue queue, cl_mem memory, T array, Pointer pointer, long byteCapacity) {
        super(memory);
        
        this.array = array;
        this.queue = queue;
        this.pointer = pointer; 
        this.cl_capacity = byteCapacity;
    }
    
    public void transferFromDevice()
    {  
        //beauty is that you don't have to reset buffer, the pointer shall always be at zero
        queue.putReadBuffer(this);
    }
    
    public void transferToDevice()
    {
        //beauty is that you don't have to reset buffer, the pointer shall always be at zero
        queue.putWriteBuffer(this);        
    }
    
    @Override
    public cl_mem getId()
    {
        return (cl_mem)super.getId();
    }
    
    public long byteCapacity()
    {
        return cl_capacity;
    }
    
    public long size()
    {
        return array.size();
    }
    
    public Pointer pointer()
    {
        return pointer;
    }
    
    public T getT()
    {
        return array;
    }
    
    public void write(Consumer<T> consume)
    {
        consume.accept(array);
        transferToDevice();
    }
    
    public void read(Consumer<T> consume)
    {
        transferFromDevice();
        consume.accept(array);
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
