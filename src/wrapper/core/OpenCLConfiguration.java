/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.structbyte.Structure;
import coordinate.struct.structbyte.StructureArray;
import coordinate.struct.structfloat.FloatStruct;
import coordinate.struct.structint.IntStruct;
import coordinate.struct.structfloat.StructFloatArray;
import coordinate.struct.structint.StructIntArray;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import wrapper.core.memory.ByteStructMemory;
import wrapper.core.memory.FloatStructMemory;
import static wrapper.core.CMemory.flagHasPointer;
import static wrapper.core.CMemory.validateMemoryType;
import static wrapper.core.CDevice.DeviceType.GPU;
import wrapper.core.memory.IntStructMemory;

/**
 *
 * @author user
 */
public class OpenCLConfiguration {
    private CPlatform platform = null;
    private CDevice device = null;        
    private CContext context = null;
    private CProgram program = null;        
    private CCommandQueue queue = null;
    
    public static OpenCLConfiguration getDefault(String... sources)
    {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        OpenCLConfiguration configuration = new OpenCLConfiguration();        
        configuration.platform = CPlatform.getFastestPlatform(GPU);
        configuration.device = configuration.platform.getDeviceGPU();
        configuration.context = configuration.platform.createContext(configuration.device);
        configuration.program = configuration.context.createProgram(sources);
        configuration.queue = configuration.context.createCommandQueue(configuration.device);
        return configuration;
    }
    
    public CDevice getDevice()
    {
        return device;
    }
        
    public<T extends FloatStruct> CMemory<T> createBufferF(Class<T> clazz, int size, long flag)
    {
        if(FloatStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
            
            StructFloatArray<T> structArray = new StructFloatArray(clazz, size);
            
            //buffer for pointer            
            FloatBuffer buffer = FloatBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_float * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            FloatStructMemory<T> memory = new FloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            return memory;
        }
        return null;
    }
    
    public<T extends FloatStruct> CMemory<T> createValueF(Class<T> clazz, T t, long flag)
    {
        if(FloatStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
            
            StructFloatArray<T> structArray = new StructFloatArray(clazz, 1);
            structArray.set(t, 0);
            
            //buffer for pointer            
            FloatBuffer buffer = FloatBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_float * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            FloatStructMemory<T> memory = new FloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    public<T extends FloatStruct> CMemory<T> createFromF(Class<T> clazz, float[] array, long flag)
    {
        if(FloatStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
                        
            StructFloatArray<T> structArray = new StructFloatArray(clazz, array);
            
            //buffer for pointer            
            FloatBuffer buffer = FloatBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_float * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            FloatStructMemory<T> memory = new FloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    public<T extends IntStruct> CMemory<T> createBufferI(Class<T> clazz, int size, long flag)
    {
        if(IntStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
            
            StructIntArray<T> structArray = new StructIntArray(clazz, size);
            
            //buffer for pointer            
            IntBuffer buffer = IntBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_int * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            IntStructMemory<T> memory = new IntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            return memory;
        }
        return null;
    }
    
    public<T extends IntStruct> CMemory<T> createValueI(Class<T> clazz, T t, long flag)
    {
        if(IntStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
            
            StructIntArray<T> structArray = new StructIntArray(clazz, 1);
            structArray.set(t, 0);
            
            //buffer for pointer            
            IntBuffer buffer = IntBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_int * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            IntStructMemory<T> memory = new IntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    public<T extends IntStruct> CMemory<T> createFromI(Class<T> clazz, int[] array, long flag)
    {
        if(IntStruct.class.isAssignableFrom(clazz))
        {
            //validate memory type for opencl
            validateMemoryType(flag);
                        
            StructIntArray<T> structArray = new StructIntArray(clazz, array);
            
            //buffer for pointer            
            IntBuffer buffer = IntBuffer.wrap(structArray.getArray());        
            Pointer pointer = Pointer.to(buffer); 
            long clSize = Sizeof.cl_int * structArray.getArraySize(); 
            cl_mem clMem;
            
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            IntStructMemory<T> memory = new IntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    public<T extends Structure> CMemory<T> createBufferB(Class<T> clazz, int size, long flag)
    {
        if(Structure.class.isAssignableFrom(clazz))
        {
            
            StructureArray<T> structArray = new StructureArray(clazz, size);
            int byteArraySize = structArray.getByteArraySize();
            ByteBuffer buffer = ByteBuffer.wrap(structArray.getArray()).order(ByteOrder.nativeOrder());        
            Pointer pointer = Pointer.to(buffer); 

            cl_mem clMem;
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, null, null);

            ByteStructMemory memory = new ByteStructMemory(queue, clMem, clazz, structArray, buffer, pointer, byteArraySize); 
                        
            return memory;
        }
        return null;
    }
    
    public<T extends Structure> CMemory<T> createFromB(Class<T> clazz, StructureArray<T> structArray, long flag)
    {
        if(Structure.class.isAssignableFrom(clazz))
        {            
            int byteArraySize = structArray.getByteArraySize();
            ByteBuffer buffer = ByteBuffer.wrap(structArray.getArray()).order(ByteOrder.nativeOrder());        
            Pointer pointer = Pointer.to(buffer); 

            cl_mem clMem;
            if(flagHasPointer(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, null, null);

            ByteStructMemory memory = new ByteStructMemory(queue, clMem, clazz, structArray, buffer, pointer, byteArraySize); 
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    public CKernel createKernel(String kernelName)
    {
        return program.createKernel(kernelName);
    }
    
    public CKernel createKernel(String kernelName, CMemory... memory)
    {
        return program.createKernel(kernelName, memory);
    }
    
    public void execute1DKernel(CKernel kernel, long globalWorkSize, long localWorkSize)
    {
        queue.put1DRangeKernel(kernel, globalWorkSize, localWorkSize); 
    }
    
    public CCommandQueue finish()
    {
        return queue.finish();
    }
}
