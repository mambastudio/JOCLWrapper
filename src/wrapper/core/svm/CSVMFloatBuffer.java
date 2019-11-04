/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.svm;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.jocl.Pointer;
import wrapper.core.CCommandQueue;
import wrapper.core.CSVMBuffer;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 */
public class CSVMFloatBuffer extends CSVMBuffer
{
    private FloatBuffer buffer = null;
    
    public CSVMFloatBuffer(Pointer pointer, long cl_size) {
        super(pointer, cl_size); //cl_mem = null and buffer = null
        buffer = getSVMPointer().getByteBuffer(0, getCLSize()).order(ByteOrder.nativeOrder()).asFloatBuffer();   
    }
           
    public float get(int i)
    {          
        return buffer.get(i);
    }
    
    public float get()
    {
        return buffer.get();
    }

    public void put(float... src)
    {
        buffer.put(src);
    }
    
    public void putIntoArray(float... src)
    {
        buffer.get(src);
    }
    
    public int remaining()
    {
        return buffer.remaining();
    }
    
    public void rewind()
    {
        buffer.rewind();
    }
    
    @Override
    public String toString()
    {
        return Arrays.toString(buffer.array());
    }

    @Override
    public int getArraySize() {
        return buffer.capacity();
    }
    
    @Override
    public void writeNativeBuffer(CCommandQueue queue)
    {
        queue.mapSVM(this);
        buffer.rewind();
        queue.unmapSVM(this);
    }

    @Override
    public void readNativeBuffer(CCommandQueue queue) {
        queue.mapSVM(this);
        buffer.rewind();
        queue.unmapSVM(this);        
    }
    
    public void mapWriteBuffer(CCommandQueue queue, CallBackFunction<FloatBuffer> function)
    {
        function.call(buffer);
        writeNativeBuffer(queue);
    }
    
    public void mapReadBuffer(CCommandQueue queue, CallBackFunction<FloatBuffer> function)
    {
        readNativeBuffer(queue);
        function.call(buffer);        
    }
}
