/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.ByteStruct;
import coordinate.struct.StructByteArray;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jocl.CL;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clSVMAlloc;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import org.jocl.struct.Buffers;
import org.jocl.struct.SizeofStruct;
import org.jocl.struct.Struct;
import static wrapper.core.CMemory.COPY_HOST_PTR;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.READ_WRITE;
import static wrapper.core.CMemory.WRITE_ONLY;
import wrapper.core.buffer.CByteBuffer;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;
import wrapper.core.buffer.CStructBuffer;
import wrapper.core.buffer.CStructTypeBuffer;
import wrapper.core.svm.CSVMFloatBuffer;
import wrapper.core.svm.CSVMIntBuffer;
import wrapper.core.svm.CSVMStructBuffer;

/**
 *
 * @author user
 * 
 *  
 */
public class CBufferFactory 
{
    public static CByteBuffer allocByte(String name, CContext context, int size, long flag)
    {
        validate(flag);
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());        
        Pointer ptr = Pointer.to(buffer);         
        long clSize = size;        
        cl_mem clMem = clCreateBuffer(context.getId(), flag, clSize, null, null);
        CByteBuffer cbuffer =  new CByteBuffer(clMem, buffer, ptr, clSize); 
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer; 
    }
    
    public static CFloatBuffer allocFloat(String name, CContext context, int size, long flag)
    {
        validate(flag);
        FloatBuffer buffer = FloatBuffer.allocate(size);        
        Pointer ptr = Pointer.to(buffer);         
        long clSize = Sizeof.cl_float * size;        
        cl_mem clMem = clCreateBuffer(context.getId(), flag, clSize, null, null);
        CFloatBuffer cbuffer =  new CFloatBuffer(clMem, buffer, ptr, clSize); 
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer; 
    }
    
    public static CFloatBuffer initFloatValue(String name, CContext context, CCommandQueue queue, float value, long flag)
    {
        CFloatBuffer cbuffer = allocFloat(name, context, 1, flag);
        cbuffer.mapWriteBuffer(queue, buffer -> buffer.put(0, value));
        return cbuffer;
    }
    
    public static CFloatBuffer wrapFloat(String name, CContext context, CCommandQueue queue, float[] array, long flag)
    {
        validate(flag);
        if(array == null)
            throw new UnsupportedOperationException("array is null");            
        FloatBuffer buffer = FloatBuffer.wrap(array);
        Pointer ptr = Pointer.to(buffer);         
        long clSize = Sizeof.cl_float * buffer.capacity();        
        cl_mem clMem = clCreateBuffer(context.getId(), flag, clSize, null, null);
        
        //create and transfer data to GPU
        CFloatBuffer cbuffer = new CFloatBuffer(clMem, buffer, ptr, clSize);
        cbuffer.transferFromBufferToDevice(queue);        
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer; 
    }
    
    public static CIntBuffer allocInt(String name, CContext context, int size, long flag)
    {
        validate(flag);
        IntBuffer buffer = IntBuffer.allocate(size);        
        Pointer ptr = Pointer.to(buffer);         
        long clSize = Sizeof.cl_int * buffer.capacity();        
        cl_mem clMem = clCreateBuffer(context.getId(), flag, clSize, null, null);    
        CIntBuffer cbuffer = new CIntBuffer(clMem, buffer, ptr, clSize);    
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer;    
    }
    
    public static CIntBuffer initIntValue(String name, CContext context, CCommandQueue queue, int value, long flag)
    {
        CIntBuffer cbuffer = allocInt(name, context, 1, flag);
        cbuffer.mapWriteBuffer(queue, buffer -> buffer.put(0, value));
        return cbuffer;
    }
    
    public static CIntBuffer wrapInt(String name, CContext context, CCommandQueue queue, int[] array, long flag)
    {
        validate(flag);
        if(array == null)
            throw new UnsupportedOperationException("array is null");            
        IntBuffer buffer = IntBuffer.wrap(array);
        Pointer ptr = Pointer.to(buffer);         
        long clSize = Sizeof.cl_int * buffer.capacity();        
        cl_mem clMem = clCreateBuffer(context.getId(), flag, clSize, null, null);
                
        //create and transfer data to GPU
        CIntBuffer cbuffer = new CIntBuffer(clMem, buffer, ptr, clSize);
        cbuffer.transferFromBufferToDevice(queue);       
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer; 
    }
    
    public static <B extends ByteStruct> CStructTypeBuffer<B> allocStructType(String name, CContext context, Class<B> clazz, int size, long flag)
    {
        StructByteArray<B> structArray = new StructByteArray(clazz, size);
        int byteArraySize = structArray.getByteArraySize();
        ByteBuffer buffer = ByteBuffer.wrap(structArray.getArray()).order(ByteOrder.nativeOrder());        
        Pointer pointer = Pointer.to(buffer); 
        
        cl_mem clMem;
        if(flagHasPointer(flag))
            clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, pointer, null);        
        else
            clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, null, null);
        
        CStructTypeBuffer<B> cbuffer = new CStructTypeBuffer(clMem, structArray, buffer, pointer, byteArraySize);
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer;
    }
    
    public static <B extends ByteStruct> CStructTypeBuffer<B> allocStructType(String name, CContext context, StructByteArray structArray, int size, long flag)
    {
        int byteArraySize = structArray.getByteArraySize();
        ByteBuffer buffer = ByteBuffer.wrap(structArray.getArray()).order(ByteOrder.nativeOrder());        
        Pointer pointer = Pointer.to(buffer); 
        
        cl_mem clMem;
        if(flagHasPointer(flag))
            clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, pointer, null);        
        else
            clMem = CL.clCreateBuffer(context.getId(), flag, byteArraySize, null, null);
        
        CStructTypeBuffer<B> cbuffer = new CStructTypeBuffer(clMem, structArray, buffer, pointer, byteArraySize);
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer;
    }
    
    public static <B extends Struct> CStructBuffer<B> allocStruct(String name, CContext context, Class<B> structClass, int size, long flag)
    {
        validate(flag);
        long structSize = SizeofStruct.sizeof(structClass) * size;
        B[] array = (B[])Array.newInstance(structClass, size);        
        for(int i = 0; i<size; i++)
        {
            try {
                array[i] = structClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(CStructBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ByteBuffer structBuffer = Buffers.allocateBuffer(array);
        Pointer pointer = Pointer.to(structBuffer); 
        
        cl_mem clMem;
        
        if(flagHasPointer(flag))
            clMem = CL.clCreateBuffer(context.getId(), flag, structSize, pointer, null);        
        else
            clMem = CL.clCreateBuffer(context.getId(), flag, structSize, null, null);
        CStructBuffer<B> cbuffer = new CStructBuffer(clMem, array, structBuffer, pointer, structSize, flag);
        CResourceFactory.registerMemory(name, cbuffer);
        return cbuffer;
    }
    
   // public static<C extends StructType> 

    
    public static CSVMIntBuffer allocSVMInt(CContext context, int size, long flag)
    {            
        validate(flag);
        long clSize = Sizeof.cl_int * size;        //opencl size        
        Pointer svm = clSVMAlloc(context.getId(), flag, clSize, 0);          
        return new CSVMIntBuffer(svm, clSize);
    }
    
    public static CSVMFloatBuffer allocSVMFloat(CContext context, int size, long flag)
    {                
        validate(flag);
        long clSize = Sizeof.cl_float * size;        //opencl size        
        Pointer svm = clSVMAlloc(context.getId(), flag, clSize, 0);          
        return new CSVMFloatBuffer(svm, clSize);
    }
    
    public static <B extends Struct> CSVMStructBuffer<B> allocSVMStruct(CContext context, Class<B> structClass, int size, long flag)
    {
        validate(flag);
        //Create struct array pointer and buffer
        long clSize = SizeofStruct.sizeof(structClass) * size; //opencl size              
        Pointer svm = clSVMAlloc(context.getId(), flag, clSize, 0);
        B[] array = (B[])Array.newInstance(structClass, size);
        
        for(int i = 0; i<size; i++)
        {
            try {
                array[i] = structClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(CSVMBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new CSVMStructBuffer<>(array, svm, clSize);
    }
    
    private static void validate(long flag)
    {
        if(!isValid(flag))
            throw new UnsupportedOperationException("flag not yet supported for now");            
    }
       
    public static boolean flagHasPointer(long flag)
    {
        return !(flag == READ_ONLY || flag == WRITE_ONLY || flag == READ_WRITE);
    }
    
    public static boolean isValid(long flag)
    {
        return  flag == READ_ONLY || 
                flag == WRITE_ONLY || 
                flag == READ_WRITE || 
                flag == (READ_ONLY|COPY_HOST_PTR) || 
                flag == (WRITE_ONLY|COPY_HOST_PTR) || 
                flag == (READ_WRITE|COPY_HOST_PTR);  
    }
    
}
