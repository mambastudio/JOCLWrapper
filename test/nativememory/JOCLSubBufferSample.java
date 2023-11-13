/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2012 Marco Hutter - http://www.jocl.org/
 */
package nativememory;

import static org.jocl.CL.*;

import java.nio.*;
import java.util.*;

import org.jocl.*;


/**
 * A sample demonstrating how to create sub-buffers
 * that have been introduced with OpenCL 1.1.
 */
public class JOCLSubBufferSample
{
    private static cl_context context;
    private static cl_command_queue commandQueue;
    private static int total_alignment_requirement;

    /**
     * The entry point of this sample
     * 
     * @param args Not used
     */
    public static void main(String args[])
    {
        simpleInitialization();
        
        // Create an array with 8 elements and consecutive values
        int fullSize = 8;
        float fullArray[] = new float[fullSize];
        for (int i=0; i<fullSize; i++)
        {
            fullArray[i] = i;
        }
        System.out.println("Full input array  : "+Arrays.toString(fullArray));
        
        // Create a buffer for the full array
        cl_mem fullMem = clCreateBuffer(context, 
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, 
            Sizeof.cl_float * fullSize, Pointer.to(fullArray), null);

        // Create a sub-buffer
        int subOffset = 2;
        int subSize = 4;
        cl_mem subMem = clCreateSubBuffer(fullMem, 
            (int)CL_MEM_READ_WRITE, CL_BUFFER_CREATE_TYPE_REGION, 
            createInfo(subOffset, subSize, Sizeof.cl_float), null);

        // Create an array for the sub-buffer, and copy the data
        // from the sub-buffer to the array
        float subArray[] = new float[subSize];
        clEnqueueReadBuffer(commandQueue, subMem, true, 
            0, subSize * Sizeof.cl_float, Pointer.to(subArray), 
            0, null, null);
        
        System.out.println("Read sub-array    : "+Arrays.toString(subArray));

        // Modify the data in the sub-array, and copy it back
        // into the sub-buffer
        subArray[0] = -5;
        subArray[1] = -4;
        subArray[2] = -3;
        subArray[3] = -2;
        clEnqueueWriteBuffer(commandQueue, subMem, true, 
            0, subSize * Sizeof.cl_float, Pointer.to(subArray), 
            0, null, null);

        System.out.println("Modified sub-array: "+Arrays.toString(subArray));
        
        // Read the full buffer back into the array 
        clEnqueueReadBuffer(commandQueue, fullMem, true, 
            0, fullSize * Sizeof.cl_float, Pointer.to(fullArray), 
            0, null, null);
        
        System.out.println("Full result array : "+Arrays.toString(fullArray));
        
    }
    
    /**
     * Create a pointer to a 'buffer_create_info' struct for the
     * {@link CL#clCreateSubBuffer(cl_mem, int, int, Pointer, int[])}
     * call
     * 
     * @param offset The sub-buffer offset, in number of elements
     * @param size The sub-buffer size, in number of elements
     * @param The size of the buffer elements (e.g. Sizeof.cl_float)
     * @return The pointer to the buffer creation info
     */
    private static Pointer createInfo(long offset, long size, int elementSize)
    {
        // The 'buffer_create_info' is a struct with two size_t
        // values on native side. This is emulated with a 
        // byte buffer of the appropriate size
        ByteBuffer createInfo = 
            ByteBuffer.allocate(2 * Sizeof.size_t).order(
                ByteOrder.nativeOrder());
        
    
        
        if (Sizeof.size_t == Sizeof.cl_int)
        {
            createInfo.putInt(0, (int)offset * elementSize); 
            createInfo.putInt(Sizeof.size_t , (int)size * elementSize);
        }
        else
        {
            createInfo.putLong(0, offset * elementSize); 
            createInfo.putLong(Sizeof.size_t , size * elementSize);
        }
        return Pointer.to(createInfo);
    }
    
    /**
     * Simple OpenCL initialization of the context and command queue
     */
    private static void simpleInitialization()
    {
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 1;
        final long deviceType = CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
                
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];
        
        long [] align = new long[1];
        CL.clGetDeviceInfo(device, CL.CL_DEVICE_MEM_BASE_ADDR_ALIGN, Sizeof.size_t, Pointer.to(align), null);
        int alignment = (int) align[0];
        
        total_alignment_requirement = 1;        
        total_alignment_requirement = lcm(total_alignment_requirement, alignment);

        // Create a context for the selected device
        context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue
        commandQueue = 
            clCreateCommandQueue(context, devices[0], 0, null);
    }
    
    private static int gcd(int a, int b)
    {
        for (;;)
        {
            if (a == 0) return b;
            b %= a;
            if (b == 0) return a;
            a %= b;
        }
    }

    private static int lcm(int a, int b)
    {
        int temp = gcd(a, b);

        return temp != 0? (a / temp * b) : 0;
    }
    
    public static long posixMemalign(int alignment, int index) {
        if (!isPowerOfTwo(alignment)) {
            throw new IllegalArgumentException("Alignment must be a power of two");
        }

        long alignedOffset = alignIndex(index, alignment);
        return alignedOffset;
    }

    private static boolean isPowerOfTwo(int value) {
        return (value & (value - 1)) == 0;
    }

    private static long alignIndex(long index, int alignment) {
        return (index + alignment - 1) & ~(alignment - 1);
    }
}