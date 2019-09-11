/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.Buffer;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.clReleaseMemObject;
import org.jocl.Pointer;
import org.jocl.cl_mem;

/**
 *
 * @author user
 * @param <B>
 */
public abstract class CMemory <B extends Buffer> extends CObject implements CResource
{    
    protected B buffer;
    protected long cl_size;
    protected Pointer pointer;
    
    boolean isReleased = false;
    
    public CMemory(cl_mem memory, B buffer, Pointer pointer, long cl_size)
    {
        super(memory);
        
        this.buffer = buffer;        
        this.cl_size = cl_size;
        this.pointer = pointer;        
    }
    
    public long getCLSize()
    {
        return cl_size;
    }
    
    public boolean hasRemaining()
    {
        return buffer.hasRemaining();
    }
    
    public Pointer getPointer()
    {
        return pointer;
    }  
    
    public B getBuffer()
    {
        return buffer;
    }
        
    @Override
    public cl_mem getId()
    {
        return (cl_mem)super.getId();
    }
    
    @Override
    public void release() 
    {
        clReleaseMemObject(getId());        
    }
    
    
    public final static long READ_WRITE = CL_MEM_READ_WRITE;
    public final static long READ_ONLY = CL_MEM_READ_ONLY;
    public final static long WRITE_ONLY = CL_MEM_WRITE_ONLY;
    public final static long COPY_HOST_PTR = CL_MEM_COPY_HOST_PTR;
    
    public final static long READ_WRITE_COPY_PTR = READ_WRITE|COPY_HOST_PTR;
    public final static long READ_COPY_PTR = READ_ONLY|COPY_HOST_PTR;
    public final static long WRITE_COPY_PTR = WRITE_ONLY|COPY_HOST_PTR;
}
