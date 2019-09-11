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
import wrapper.core.buffer.local.CLocalBuffer;

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
    
    public CKernel putArgs(CMemory<?>... values)
    {        
        for(CMemory value : values) 
        {
            if(value instanceof CLocalBuffer)
                clSetKernelArg(getId(), argIndex++, value.getCLSize(), null); 
            else
                clSetKernelArg(getId(), argIndex++, Sizeof.cl_mem, Pointer.to(value.getId()));
        }        
        return this;
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
