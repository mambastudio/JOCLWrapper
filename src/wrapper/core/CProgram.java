/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.util.ArrayList;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clReleaseProgram;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

/**
 *
 * @author user
 */
public class CProgram extends CObject implements CResource
{  
    public CProgram(cl_program prog) 
    {
        super(prog);                
    }
    
    @Override
    public cl_program getId()
    {
        return (cl_program)super.getId();
    }
    
    @Override
    public void release() {
        clReleaseProgram(getId()); 
    }
    
    public CKernel createKernel(String kernelName)
    {
        cl_kernel ck = clCreateKernel(
                getId(),
                kernelName,
                null
                );
        
        CKernel kernel = new CKernel(ck, kernelName);
        CResourceFactory.registerKernel(kernelName, kernel);       
        return kernel;
    }
    
    public CKernel createSVMKernel(String kernelName, CSVMBuffer... buffers)
    {
        CKernel kernel = createKernel(kernelName).putSVMArgs(buffers);
        CResourceFactory.registerKernel(kernelName, kernel);
        return kernel;
    }
    
    public CKernel createKernel(String kernelName, CMemory... buffers)
    {
        CKernel kernel = createKernel(kernelName).putArgs(buffers);
        CResourceFactory.registerKernel(kernelName, kernel);
        return kernel;
    }
    
    public CKernel createKernel(String kernelName, CMemorySkeleton... buffers)
    {
        CKernel kernel = createKernel(kernelName).putArgs(buffers);
        CResourceFactory.registerKernel(kernelName, kernel);
        return kernel;
    }
    
    public CKernel createKernel(String kernelName, CNativeMemory... buffers)
    {
        CKernel kernel = createKernel(kernelName).putArgs(buffers);
        CResourceFactory.registerKernel(kernelName, kernel);
        return kernel;
    }
}
