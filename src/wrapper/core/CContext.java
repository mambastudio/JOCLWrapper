/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import static org.jocl.CL.CL_SUCCESS;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clReleaseContext;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_program;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;

/**
 *
 * @author user
 */
public class CContext extends CObject
{
    private final cl_context_properties properties;
    
    private final ArrayList<CDevice> deviceList;    
    
    public CContext(cl_context id, cl_context_properties properties, cl_device_id... devices)
    {
        super(id); 
        
        this.properties = properties;                 
        this.deviceList = new ArrayList<>();
        
        for(cl_device_id deviceID: devices)
            deviceList.add(new CDevice(deviceID));
    }
            
    public ArrayList<CDevice> getDevices()
    {
        return deviceList;
    }
       
    @Override
    public cl_context getId()
    {
        return (cl_context)super.getId();
    }
    
    public CCommandQueue createCommandQueue(CDevice device)
    {
        cl_command_queue cq = clCreateCommandQueue(
                getId(),
                device.getId(),
                0, 
                null
                );
        CCommandQueue queue = new CCommandQueue(cq);
        CResourceFactory.registerCommandQueue("queue", queue);        
        return queue;        
    }    
        
    public CProgram createProgram(String... sources)
    {
        cl_program prog = clCreateProgramWithSource(getId(),
                sources.length, sources, null, null);  
        
        int build = clBuildProgram(prog, 0, null, null, null, null);
        if(build != CL_SUCCESS)
            System.out.println("program build not successful");        
        
        CProgram program = new CProgram(prog);
        CResourceFactory.registerProgram("program", program);
        return program;
    }
    
    public cl_context_properties getPropertiesId()
    {
        return properties;
    }
    
    public boolean release()
    {        
        clReleaseContext(getId());
        return true;
    }
}
