/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import org.jocl.CL;
import static org.jocl.CL.CL_DEVICE_MAX_CLOCK_FREQUENCY;
import static org.jocl.CL.CL_DEVICE_NAME;
import static org.jocl.CL.CL_DEVICE_TYPE;
import static org.jocl.CL.CL_DEVICE_TYPE_ACCELERATOR;
import static org.jocl.CL.CL_DEVICE_TYPE_CPU;
import static org.jocl.CL.CL_DEVICE_TYPE_DEFAULT;
import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.CL_DEVICE_VENDOR;
import static org.jocl.CL.clGetDeviceInfo;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import static wrapper.core.CDevice.DeviceType.CPU;
import static wrapper.core.CDevice.DeviceType.GPU;

/**
 *
 * @author user
 */
public class CDevice extends CObject
{    
    public enum DeviceType{GPU, CPU}
    
    public CDevice(cl_device_id device_id)
    {
        super(device_id);
    }
    
    @Override
    public cl_device_id getId()
    {
        return (cl_device_id)super.getId();
    }
    
    public String getName()
    {
        return getString(CL_DEVICE_NAME);
    }
    
    public String getVendor()
    {
        return getString(CL_DEVICE_VENDOR);
    }
    
    public long getSpeed()
    {
        return getLong(getId(), CL_DEVICE_MAX_CLOCK_FREQUENCY);
    }
    
    public long getMaximumWorkGroupSize()
    {
        return getLong(getId(), CL.CL_DEVICE_MAX_WORK_GROUP_SIZE);
    }
    
    public boolean isGPU()
    {
        return getType().equals("CL_DEVICE_TYPE_GPU");
    }
    
    public boolean isCPU()
    {
        return getType().equals("CL_DEVICE_TYPE_CPU");
    }
    
    public boolean isDeviceType(DeviceType type)
    {
        if(null == type)
            return false;
        else switch (type) {
            case GPU:
                return isGPU();
            case CPU:
                return isCPU();
            default:
                return false;
        }
    }
    
    public String getType()
    {
        long deviceType = getLong(getId(), CL_DEVICE_TYPE);
        if( (deviceType & CL_DEVICE_TYPE_CPU) != 0)
            return "CL_DEVICE_TYPE_CPU";
        if( (deviceType & CL_DEVICE_TYPE_GPU) != 0)
            return "CL_DEVICE_TYPE_GPU";
        if( (deviceType & CL_DEVICE_TYPE_ACCELERATOR) != 0)
            return "CL_DEVICE_TYPE_ACCELERATOR";
        if( (deviceType & CL_DEVICE_TYPE_DEFAULT) != 0)
            return "CL_DEVICE_TYPE_DEFAULT";
        else
            return "UNDEFINED";
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
    
    private String getString(int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(getId(), paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(getId(), paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }
}
