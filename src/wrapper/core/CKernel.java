/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clSetKernelArg;
import static org.jocl.CL.clSetKernelArgSVMPointer;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import wrapper.core.memory.CLocalMemory;

/**
 *
 * @author user
 */
public class CKernel extends CObject implements CResource
{
    public final String name;    
    private int argIndex = 0;
    
    public CKernel(cl_kernel id, String name)
    {
        super(id);
        this.name = name;
    }
    
    public CKernel putArg(float value)
    {
        clSetKernelArg(getId(), argIndex++, Sizeof.cl_float, Pointer.to(new float[]{value}));
        return this;
    }
    
    public CKernel putArg(int value)
    {
        clSetKernelArg(getId(), argIndex++, Sizeof.cl_int, Pointer.to(new int[]{value}));
        return this;
    }
    
    public CKernel putArg(int index, CMemory<?> value)
    {
        if(value instanceof CLocalMemory)
            clSetKernelArg(getId(), index, value.getCLSize(), null); 
        else
            clSetKernelArg(getId(), index, Sizeof.cl_mem, Pointer.to(value.getId()));
        return this;
    }
    
    public CKernel putArgs(CMemory<?>... values)
    {        
        for(CMemory value : values) 
        {            
            if(value instanceof CLocalMemory)
                clSetKernelArg(getId(), argIndex++, value.getCLSize(), null); 
            else
                clSetKernelArg(getId(), argIndex++, Sizeof.cl_mem, Pointer.to(value.getId()));
        }        
        return this;
    }
    
    public CKernel putArgs(CNativeMemory<?>... values)
    {        
        for(CNativeMemory value : values) 
        {       
            if(value instanceof CNativeMemoryLocal)
                clSetKernelArg(getId(), argIndex++, value.byteCapacity(), null); 
            else
                clSetKernelArg(getId(), argIndex++, Sizeof.cl_mem, Pointer.to(value.getId()));
        }        
        return this;
    }
    
    public CKernel putArgs(CMemorySkeleton... values)
    {        
        for(CMemorySkeleton value : values) 
        {                   
            if(value.isLocal())
                clSetKernelArg(getId(), argIndex++, value.elementSize(), null);
            else if(value.isSVM())
                clSetKernelArgSVMPointer(getId(), argIndex++, value.getPointer());
            else
                clSetKernelArg(getId(), argIndex++, value.getByteCapacity(), value.getPointer());
        }        
        return this;
    }
    
    
    public CKernel resetPutArgs(CMemory<?>... values)
    {
        argIndex = 0;
        return putArgs(values);
    }
    
    public CKernel putSVMArgs(CSVMBuffer... buffers)
    {
        for(CSVMBuffer buffer : buffers)
        {
            clSetKernelArgSVMPointer(getId(), argIndex++, buffer.getSVMPointer());
        }
        return this;
    }
    
    @Override
    public cl_kernel getId()
    {
        return (cl_kernel)super.getId();
    }

    @Override
    public void release() {
        clReleaseKernel(getId());
    }
    
}
