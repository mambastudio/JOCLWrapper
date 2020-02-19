/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.jocl.CL.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/**
 *
 * @author user
 */
public class CPlatform  extends CObject
{    
    private final cl_device_id[] device_ids;
    
    public CPlatform(cl_platform_id id)
    {
        super(id);
        
         //Obtain the number of devices for the current platform
        int numDevices[] = new int[1];
        clGetDeviceIDs(id, CL_DEVICE_TYPE_ALL, 0, null, numDevices);
        
        //Create devices        
       device_ids = new cl_device_id[numDevices[0]]; 
        
        clGetDeviceIDs(
                id,
                CL_DEVICE_TYPE_ALL,
                numDevices[0],
                device_ids,
                null
                );      
    }
    
    public String getInfo(int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(getId(), paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(getId(), paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }
   
    public static CPlatform getFirst() 
    {
        return getPlatforms().stream().findFirst().orElse(null);
    }
    
    public static List<CPlatform> getPlatforms()
    {
        cl_platform_id platforms[] = new cl_platform_id[getNumberOfPlatforms()];
        clGetPlatformIDs(platforms.length, platforms, null);
        return Arrays.stream(platforms).map(x -> new CPlatform(x)).collect(Collectors.toList());	
    }
    
    public static int getNumberOfPlatforms()
    {
        final int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        return numPlatformsArray[0];
    }
    
    public ArrayList<CDevice> getAllDevices()
    {
        ArrayList<CDevice> devices = new ArrayList<>();
        for(cl_device_id device_id : device_ids)
            devices.add(new CDevice(device_id));
        return devices;
    }
    
    public cl_device_id[] getAllDevicesIDs()
    {
        return device_ids;
    }
    
    public CDevice getDefaultDevice()
    {        
        return new CDevice(device_ids[0]);
    }
    
    public CDevice getDevice(int deviceNo)
    {
        return new CDevice(device_ids[deviceNo]);
    }
    
    public CDevice getDeviceCPU()
    {
        int[] numGPUDevice = new int[1];
        clGetDeviceIDs(getId(), CL_DEVICE_TYPE_CPU, 0, null, numGPUDevice);
        
        cl_device_id [] gpu_ids = new cl_device_id[numGPUDevice[0]];
        clGetDeviceIDs(
                getId(),
                CL_DEVICE_TYPE_CPU,
                numGPUDevice[0],
                gpu_ids,
                null
                );  
        
        return new CDevice(gpu_ids[0]);
    }
    
    public CDevice getDeviceGPU()
    {
        int[] numGPUDevice = new int[1];
        clGetDeviceIDs(getId(), CL_DEVICE_TYPE_GPU, 0, null, numGPUDevice);
        
        cl_device_id [] gpu_ids = new cl_device_id[numGPUDevice[0]];
        clGetDeviceIDs(
                getId(),
                CL_DEVICE_TYPE_GPU,
                numGPUDevice[0],
                gpu_ids,
                null
                );  
        
        return new CDevice(gpu_ids[0]);
    }
    
    public CDevice getFastestDeviceGPU()
    {
        int[] numGPUDevice = new int[1];
        clGetDeviceIDs(getId(), CL_DEVICE_TYPE_GPU, 0, null, numGPUDevice);
        
        cl_device_id [] gpu_ids = new cl_device_id[numGPUDevice[0]];
        clGetDeviceIDs(
                getId(),
                CL_DEVICE_TYPE_GPU,
                numGPUDevice[0],
                gpu_ids,
                null
                );  
        
        cl_device_id gpu_max = gpu_ids[0];
        for(cl_device_id gpu : gpu_ids)                   
            if(getLong(gpu, CL_DEVICE_MAX_CLOCK_FREQUENCY) > getLong(gpu_max, CL_DEVICE_MAX_CLOCK_FREQUENCY))
                gpu_max = gpu;
        return new CDevice(gpu_max); 
    }
    
    @Override
    public cl_platform_id getId()
    {
        return (cl_platform_id)super.getId();
    }
        
    private cl_context_properties create_cl_context_properties()
    {
        cl_context_properties properties = new cl_context_properties();
        properties.addProperty(CL_CONTEXT_PLATFORM, getId());
        return properties;
    }
    
    public CContext createContext(cl_device_id... devices)
    {
        cl_context_properties properties = create_cl_context_properties();        
        cl_context ct = clCreateContext(
                properties,
                devices.length,
                devices,
                null,
                null,
                null
                );
        CContext context = new CContext(ct, properties, devices);
        CResourceFactory.registerContext("context", context);
        return context;
    }
    
    public CContext createContext(CDevice... devices)
    {
        cl_device_id[] ids = new cl_device_id[devices.length];
        for(int i = 0; i<devices.length; i++)
            ids[i] = devices[i].getId();
        return createContext(ids);
    }
    
    public int getDeviceNo()
    {
        return device_ids.length;
    }
       
    private long getLong(cl_device_id device, int paramName)
    {
        return getLongs(device, paramName, 1)[0];
    }

    private long[] getLongs(cl_device_id device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }
}
