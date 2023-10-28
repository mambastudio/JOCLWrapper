/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import org.jocl.CL;
import static org.jocl.CL.CL_SUCCESS;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clEnqueueWriteBuffer;
import static org.jocl.CL.clFinish;
import static org.jocl.CL.clFlush;
import static org.jocl.CL.clReleaseCommandQueue;
import org.jocl.cl_command_queue;

/**
 *
 * @author user
 */
public class CCommandQueue extends CObject implements CResource
{    
    public CCommandQueue(cl_command_queue id)
    {
        super(id);
    }
    
    public CCommandQueue putReadBuffer(CMemory readBuffer)
    {         
        int state = clEnqueueReadBuffer(getId(), readBuffer.getId(), CL_TRUE,
                0, readBuffer.getCLSize(), readBuffer.getPointer(), 0, null, null);
        
        if(state != CL_SUCCESS)           
            System.out.println("unable to read buffer successful");
       
        return this;
    }
    
    public CCommandQueue putReadBuffer(CNativeMemory readBuffer)
    {         
        int state = clEnqueueReadBuffer(getId(), readBuffer.getId(), CL_TRUE,
                0, readBuffer.byteCapacity(), readBuffer.pointer(), 0, null, null);
        
        if(state != CL_SUCCESS)           
            System.out.println("unable to read buffer successful");
       
        return this;
    }
    
    
    public CCommandQueue putWriteBuffer(CMemory writeBuffer)
    {
        int state = clEnqueueWriteBuffer(getId(), writeBuffer.getId(), true,
                0, writeBuffer.getCLSize(), writeBuffer.getPointer(), 0, null, null);
        
        if(state != CL_SUCCESS)            
            System.out.println("unable to write buffer successful");
        
        return this;
    }
    
    public CCommandQueue putWriteBuffer(CNativeMemory writeBuffer)
    {
        int state = clEnqueueWriteBuffer(getId(), writeBuffer.getId(), true,
                0, writeBuffer.byteCapacity(), writeBuffer.pointer(), 0, null, null);
        
        if(state != CL_SUCCESS)            
            System.out.println("unable to write buffer successful");
        
        return this;
    }
            
    public CCommandQueue put1DRangeKernel(CKernel kernel, long globalWorkSize, long localWorkSize)
    {
        int state = clEnqueueNDRangeKernel(getId(), kernel.getId(), 1, null,
                new long[]{globalWorkSize}, new long[]{localWorkSize}, 0, null, null);
        
        if(state != CL_SUCCESS)            
            System.out.println("unable to execute kernel");
        
        return this;
    }
    
    public CCommandQueue put2DRangeKernel(CKernel kernel, long globalWorkSizeX, long globalWorkSizeY, long localWorkSizeX, long localWorkSizeY)
    {
        int state = clEnqueueNDRangeKernel(getId(), kernel.getId(), 2, null,
                new long[]{globalWorkSizeX, globalWorkSizeY}, new long[]{localWorkSizeX, localWorkSizeY}, 0, null, null);
        
        if(state != CL_SUCCESS)            
            System.out.println("unable to execute kernel");
        
        return this;
    }
    
     public CCommandQueue finish()
    {
        clFinish(getId());
        return this;
    }
     
     public CCommandQueue flush()
     {       
        clFlush(getId());
        return this;
     }

    @Override
    public void release() {
        clReleaseCommandQueue(getId()); 
    }
    
    @Override
    public cl_command_queue getId()
    {
        return (cl_command_queue)super.getId();
    }
    
    public CCommandQueue mapSVM(CSVMBuffer buffer)
    {
        int state = CL.clEnqueueSVMMap(getId(), true, CL.CL_MAP_WRITE, buffer.getSVMPointer(), buffer.getCLSize(), 0, null, null);
        
        if(state != 0)
            System.out.println("unable to read buffer successful");
        
        return this;
    }
    
    public CCommandQueue unmapSVM(CSVMBuffer buffer)
    {
        int state = CL.clEnqueueSVMUnmap(getId(), buffer.getSVMPointer(), 0, null, null);
        
        if(state != 0)
            System.out.println("unable to read buffer successful");
        
        return this;
    }
}
