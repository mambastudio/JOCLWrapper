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
import wrapper.core.CDevice.DeviceType;
import static wrapper.core.CDevice.DeviceType.CPU;
import static wrapper.core.CDevice.DeviceType.GPU;

/**
 *
 * @author user
 */
public class CPlatform  extends CObject
{    
    private final List<CDevice> devices;
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
        devices = new ArrayList<>();
        for(cl_device_id device_id: device_ids)
            devices.add(new CDevice(device_id));
    }
    
    public static CPlatform getFirst() 
    {
        return getPlatforms().stream().findFirst().orElse(null);
    }
    
    public static CPlatform getFastestPlatform(DeviceType deviceType)
    {
        CPlatform fastestPlatform = null;
        List<CPlatform> platforms = getPlatforms(); 
        long maxClock = 0;
        
        for(CPlatform platform : platforms)
        {
            List<CDevice> deviceList = platform.getDevices(deviceType);
            for(CDevice device : deviceList)
            {
                if(device.getSpeed() > maxClock)
                {
                    maxClock = device.getSpeed(); 
                    fastestPlatform = platform;
                }
            }
        }
        
        return fastestPlatform;
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
    
    public List<CDevice> getDevices()
    {
        return devices;
    }
    
    public List<CDevice> getDevices(DeviceType type)
    {
        if(null == type)
            return null;
        else switch (type) {
            case CPU:
                return getCPUDevices();
            case GPU:
                return getGPUDevices();
            default:
                return null;
        }
    }
    
    public List<CDevice> getGPUDevices()
    {
        List<CDevice> gpuDevices = new ArrayList<>();
        devices.stream().filter((device) -> (device.isGPU())).forEachOrdered((device) -> {
            gpuDevices.add(device);
        });
        return gpuDevices;
    }
    
    public List<CDevice> getCPUDevices()
    {
        List<CDevice> gpuDevices = new ArrayList<>();
        devices.stream().filter((device) -> (device.isCPU())).forEachOrdered((device) -> {
            gpuDevices.add(device);
        });
        return gpuDevices;
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
        int[] numCPUDevice = new int[1];
        clGetDeviceIDs(getId(), CL_DEVICE_TYPE_CPU, 0, null, numCPUDevice);
        
        cl_device_id [] cpu_ids = new cl_device_id[numCPUDevice[0]];
        clGetDeviceIDs(
                getId(),
                CL_DEVICE_TYPE_CPU,
                numCPUDevice[0],
                cpu_ids,
                null
                );  
        
        return new CDevice(cpu_ids[0]);
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
        List<CDevice> gpuDevices = getGPUDevices();
        CDevice fastestGPU = null;
        long maxClock = 0;
        for(CDevice device : gpuDevices)
            if(device.getSpeed() > maxClock)
            {
                fastestGPU = device;
                maxClock = device.getSpeed();
            }
        return fastestGPU;
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
