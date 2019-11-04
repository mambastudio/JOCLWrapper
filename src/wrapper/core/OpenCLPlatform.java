/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.Buffer;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.core.buffer.CIntBuffer;

/**
 *
 * @author user
 */
public class OpenCLPlatform 
{
    private CPlatform platform = null;
    private CDevice device = null;        
    private CContext context = null;
    private CProgram program = null;        
    private CCommandQueue queue = null;
    
    public static OpenCLPlatform getDefault(String... sources)
    {
        OpenCLPlatform configuration = new OpenCLPlatform();        
        configuration.platform = CPlatform.getFirst();
        configuration.device = configuration.platform.getDeviceGPU();
        configuration.context = configuration.platform.createContext(configuration.device);
        configuration.program = configuration.context.createProgram(sources);
        configuration.queue = configuration.context.createCommandQueue(configuration.device);
        return configuration;
    }
    
    public CContext context() {return context;}
    public CProgram program() {return program;}
    public CCommandQueue queue() {return queue;}
    public CDevice device() {return device;}
    
    public CFloatBuffer allocFloat(String name, int size, long flag)
    {
        CFloatBuffer cbuffer = CBufferFactory.allocFloat(name, context, size, flag);
        mapWriteBuffer(cbuffer, buffer -> {});  //Initialize buffer
        return cbuffer;
    }
    
    public CIntBuffer allocInt(String name, int size, long flag)
    {
        CIntBuffer cbuffer = CBufferFactory.allocInt(name, context, size, flag);
        mapWriteBuffer(cbuffer, buffer -> {});  //Initialize buffer
        return cbuffer;
    }
    
    public CFloatBuffer allocFloatValue(String name, float value, long flag)
    {
        return CBufferFactory.initFloatValue(name, context, queue, value, flag);
    }
    
    public CIntBuffer allocIntValue(String name, int value, long flag)
    {
        return CBufferFactory.initIntValue(name, context, queue, value, flag);
    }
    
    public CFloatBuffer createFromFloatArray(String name, long flag, float... array)
    {
        return CBufferFactory.wrapFloat(name, context, queue, array, flag);
    }
    
    public CIntBuffer createFromIntArray(String name, long flag, int... array)
    {
        return CBufferFactory.wrapInt(name, context, queue, array, flag);
    }
    
    public int[] getIntArrayFromDevice(CIntBuffer cbuffer)
    {
        this.mapReadBuffer(cbuffer, buffer -> {});        
        return cbuffer.getBuffer().array();
    }
    
    public float[] getFloatArrayFromDevice(CFloatBuffer cbuffer)
    {
        this.mapReadBuffer(cbuffer, buffer -> {});        
        return cbuffer.getBuffer().array();
    }
    
    public int getIntValueFromDevice(CIntBuffer cbuffer)
    {
        this.mapReadBuffer(cbuffer, buffer -> {});        
        return cbuffer.getBuffer().array()[0];
    }
    
    public void setIntValue(int value, CIntBuffer cbuffer)
    {
        this.mapWriteBuffer(cbuffer, buffer -> buffer.put(0, value));
    }
    
    public void setFloatValue(float value, CFloatBuffer cbuffer)
    {
        this.mapWriteBuffer(cbuffer, buffer -> buffer.put(0, value));
    }
    
    public float getFloatValueFromDevice(CFloatBuffer cbuffer)
    {
        this.mapReadBuffer(cbuffer, buffer -> {});        
        return cbuffer.getBuffer().array()[0];
    }
    
    public void readFromDevice(CIntBuffer cbuffer)
    {
        mapReadBuffer(cbuffer, buffer -> {});
    }
    
    public void readFromDevice(CFloatBuffer cbuffer)
    {
        mapReadBuffer(cbuffer, buffer -> {});
    }
    
    public void writeBackToDevice(CIntBuffer cbuffer)
    {
        mapWriteBuffer(cbuffer, buffer -> {});
    }
    
    public void writeBackToDevice(CFloatBuffer cbuffer)
    {
        mapWriteBuffer(cbuffer, buffer -> {});
    }
    
    public<Q extends Buffer, R extends CBufferMemory<Q>> void mapReadBuffer(R r, CallBackFunction<Q> function)
    {
        r.mapReadBuffer(queue, function);
    }
    
    public<Q extends Buffer, R extends CBufferMemory<Q>> void mapWriteBuffer(R r, CallBackFunction<Q> function)
    {
        r.mapWriteBuffer(queue, function);
    }
    
    public CKernel createKernel(String kernelName, CMemory<?>... values)
    {
        return program.createKernel(kernelName, values);
    }
    
    public void executeKernel1D(CKernel kernel, long globalWorkSize, long localWorkSize)
    {
        queue.put1DRangeKernel(kernel, globalWorkSize, localWorkSize);
    }
}
