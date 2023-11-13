/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.StructBase;
import coordinate.utility.BitUtility;
import java.util.function.Consumer;
import org.jocl.CL;
import static org.jocl.CL.CL_BUFFER_CREATE_TYPE_REGION;
import static org.jocl.CL.CL_MEM_ALLOC_HOST_PTR;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_USE_HOST_PTR;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.clReleaseMemObject;
import org.jocl.Pointer;
import org.jocl.cl_buffer_region;
import org.jocl.cl_mem;

/**
 *
 * @author jmburu
 * @param <S>
 */
public class CNativeMemory<S extends StructBase> extends CObject {
    
    protected final MemoryStruct<S> memory; 
    protected CCommandQueue queue;
    
    protected long cl_capacity;
    protected Pointer pointer;
    
    private boolean isSubBuffer;
    
    public CNativeMemory(CCommandQueue queue, cl_mem memory, MemoryStruct<S> array, Pointer pointer, long byteCapacity) {
        super(memory);
        
        this.memory = array;
        this.queue = queue;
        this.pointer = pointer; 
        this.cl_capacity = byteCapacity;
        this.isSubBuffer = false;
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
        return memory.size();
    }
    
    public Pointer pointer()
    {
        return pointer;
    }
    
    public MemoryStruct<S> getMemory()
    {
        return memory;
    }
    
    public void write(Consumer<MemoryStruct<S>> consume)
    {
        consume.accept(memory);
        transferToDevice();
    }
    
    public void read(Consumer<MemoryStruct<S>> consume)
    {
        transferFromDevice();
        consume.accept(memory);
    }
    
    public void release()
    {
        clReleaseMemObject(getId());      
        memory.dispose();
    }
    
    public CNativeMemory<S> getSubBuffer(long offsetIndex, long flag)
    {
        if(isSubBuffer)
            throw new UnsupportedOperationException("This is already a sub memory, another one cannot be created from it!");
        
        MemoryStruct<S> subMemory = memory.offsetIndex(offsetIndex);
        
        long offsetBytes = memory.getOffsetByte(offsetIndex); 
        long sizeBytes = subMemory.getMemory().byteCapacity();
        
        // Align the offset and size
        long alignedOffset = offsetBytes;
        long alignedSize = sizeBytes;
        
        cl_buffer_region region = new cl_buffer_region(alignedOffset, alignedSize);
        
        cl_mem memRegion = CL.clCreateSubBuffer(this.getId(), flag, CL_BUFFER_CREATE_TYPE_REGION, region, null);
        
        Pointer subPointer = Pointer.to(subMemory.getDirectByteBufferPoint());
        
        CNativeMemory<S> subNativeMemory = new CNativeMemory(queue, memRegion, subMemory, subPointer, sizeBytes);
        subNativeMemory.isSubBuffer = true;
        return subNativeMemory;
    }
    
    private long aligned_offset(long address, long alignment)
    {
        // Adjust the address to the next aligned address
        long alignedAddress = (address + alignment - 1) & ~(alignment - 1);

        // Calculate the offset from the aligned address
        long offset = alignedAddress - address;
        return offset;
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
