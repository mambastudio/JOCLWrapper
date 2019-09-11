/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.svm;

import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.jocl.Pointer;
import wrapper.core.CCommandQueue;
import wrapper.core.CSVMBuffer;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 */
public class CSVMIntBuffer extends CSVMBuffer
{
    private IntBuffer buffer = null;
    
    public CSVMIntBuffer(Pointer pointer, long cl_size) {
        super(pointer, cl_size); //cl_mem = null and buffer = null
        buffer = getSVMPointer().getByteBuffer(0, getCLSize()).order(ByteOrder.nativeOrder()).asIntBuffer();   
    }
           
    public float get(int i)
    {        
        return buffer.get(i);
    }
    
    public float get()
    {
        return buffer.get();
    }
    
    public void set(int index, int value)
    {
        buffer.put(index, value);
    }

    public void put(int... src)
    {
        buffer.put(src);
    }
    
    public void putInto(int... src)
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
       
    public int[] getArray()
    {
        int[] intarray = new int[buffer.capacity()];
        buffer.get(intarray);
        return intarray;
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
    public void writeNativeBuffer(CCommandQueue queue) {
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
    
     
    public void mapWriteBuffer(CCommandQueue queue, CallBackFunction<IntBuffer> function)
    {
        queue.mapSVM(this);
        function.call(buffer);
        buffer.rewind();
        queue.unmapSVM(this);
        
    }
    
    public void mapReadBuffer(CCommandQueue queue, CallBackFunction<IntBuffer> function)
    {
        queue.mapSVM(this);        
        function.call(buffer); 
        buffer.rewind();
        queue.unmapSVM(this);
    }
}
