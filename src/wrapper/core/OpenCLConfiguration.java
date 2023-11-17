/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.StructBase;
import coordinate.struct.StructAbstractCache;
import coordinate.struct.StructAbstractMemory;
import coordinate.struct.StructUtils;
import coordinate.struct.structfloat.FloatStruct;
import coordinate.struct.structint.IntStruct;
import coordinate.struct.structfloat.StructFloatArray;
import coordinate.struct.structint.StructIntArray;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import wrapper.core.memory.CStructureMemory;
import wrapper.core.memory.CFloatStructMemory;
import static wrapper.core.CMemory.validateMemoryType;
import static wrapper.core.CDevice.DeviceType.GPU;
import wrapper.core.image.CImage;
import wrapper.core.memory.CIntStructMemory;
import wrapper.util.CLOptions;

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
    
    public static OpenCLConfiguration getDefault(CLOptions options, String... sources)
    {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        OpenCLConfiguration configuration = new OpenCLConfiguration();        
        configuration.platform = CPlatform.getFastestPlatform(GPU);
        configuration.device = configuration.platform.getDeviceGPU();
        configuration.context = configuration.platform.createContext(configuration.device);
        configuration.program = configuration.context.createProgram(options, sources);
        configuration.queue = configuration.context.createCommandQueue(configuration.device);
        return configuration;
    }
    
    public CDevice getDevice()
    {
        Objects.requireNonNull(device);
        return device;
    }
    
    public CContext getContext()
    {
        Objects.requireNonNull(context);
        return context;
    }
    
    public CCommandQueue getQueue()
    {
        Objects.requireNonNull(queue);
        return queue;
    }
    
    public<T extends StructBase> SVMNative<T> createSVM(T t, long size)
    {
        return new SVMNative(queue, context, t, size);
    }
    
    public<T extends StructBase> SVMNative<T> createSVMValue(T t)
    {
        SVMNative<T> s = new SVMNative(queue, context, t, 1);
        s.write(t);
        return s;
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CFloatStructMemory<T> memory = new CFloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CFloatStructMemory<T> memory = new CFloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CFloatStructMemory<T> memory = new CFloatStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CIntStructMemory<T> memory = new CIntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            return memory;
           
        }
        return null;
    }
    
    public<S extends StructBase> CNativeMemory<S> createBufferNative(MemoryStruct<S> value, long flag) 
    {        
        //validate memory type for opencl
        validateMemoryType(flag);

         //buffer for pointer                    
        Pointer pointer = Pointer.to(value.getDirectByteBufferPoint()); 
        long clSize = value.getMemory().byteCapacity(); 
        cl_mem clMem;

        if(isFlagHostPtr(flag))
            clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
        else
            clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
        
        return new CNativeMemory(queue, clMem, value, pointer, clSize);        
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CIntStructMemory<T> memory = new CIntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
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
            
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, pointer, null);        
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, clSize, null, null);
            
            CIntStructMemory<T> memory = new CIntStructMemory(queue, clMem, clazz, structArray, buffer, pointer, clSize);
            memory.transferToDevice();
            return memory;
        }
        return null;
    }
    
    
    public<T extends StructAbstractMemory<GlobalBuffer>, GlobalBuffer, CacheType extends StructAbstractCache<T, GlobalBuffer>> CMemory<T> 
        createBufferB(Class<T> clazzStruct, Class<CacheType> clazzCache, long size, long flag)
    {
        StructAbstractCache<T, GlobalBuffer> structArray = StructUtils.createStructCache(clazzStruct, clazzCache, size);
        if(structArray != null)
        {                        
            long byteArraySize = structArray.getByteBufferSize();            
            ByteBuffer buffer = structArray.getByteBuffer();
            buffer.clear();
            
            Pointer pointer = Pointer.to(buffer);      

            cl_mem clMem;
            if(isFlagHostPtr(flag))
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, pointer, null);     
            else
                clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, null, null);  
            
            CStructureMemory memory = new CStructureMemory(queue, clMem, clazzCache, structArray, buffer, pointer, byteArraySize, flag); 
                        
            return memory;
        }
        return null;
    } 
        
    public void writeImage(CImage image)
    {
        
    }
    
    public CKernel createKernel(String kernelName)
    {
        return program.createKernel(kernelName);
    }
    
    public CKernel createKernel(String kernelName, CMemory... memory)
    {
        return program.createKernel(kernelName, memory);
    }
    
    public CKernel createKernel(String kernelName, CNativeMemory... memory)
    {
        return program.createKernel(kernelName, memory);
    }
    
    public CKernel createKernel(String kernelName, CMemorySkeleton... memory)
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
    
    public CCommandQueue flush()
    {
        return queue.flush();
    }
    
    private static boolean isFlagHostPtr(long value)
    {
        byte a = getBit(3, value); //CL_MEM_USE_HOST_PTR
        byte b = getBit(4, value); //CL_MEM_ALLOC_HOST_PTR
        byte c = getBit(5, value); //CL_MEM_COPY_HOST_PTR
        return a == 1 || b == 1 || c == 1;
    }
    
    //https://stackoverflow.com/questions/9354860/how-to-get-the-value-of-a-bit-at-a-certain-position-from-a-byte
    private static byte getBit(int position, long ID)
    {
       return (byte) ((ID >> position) & 1);
    }    
}
