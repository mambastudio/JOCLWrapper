/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory;

import coordinate.struct.structbyte.Structure;
import coordinate.struct.structbyte.StructureArray;
import wrapper.core.CMemory;
import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 * @param <T>
 */
public class ByteStructMemory<T extends Structure> extends CMemory<T>  {
    
    StructureArray<T> structArray;
    
    public ByteStructMemory(CCommandQueue queue, cl_mem memory, Class<T> clazz, StructureArray<T> structArray, Buffer buffer, Pointer pointer, long cl_size) {
        super(queue, memory, clazz, buffer, pointer, cl_size);
        this.structArray = structArray;
    }
    
    @Override
    public void setCL(T t)
    {
        buffer.clear(); //reset buffer
        structArray.set(t, 0);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        buffer.rewind(); 
    }
    
    
    @Override
    public T getCL() {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        return structArray.get(0);
    }


    @Override
    public void mapReadIterator(CallBackFunction<Iterable<T>> function) {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        function.call(structArray);
    }
    

    @Override
    public void mapWriteIterator(CallBackFunction<Iterable<T>> function) {
        buffer.clear(); //reset buffer
        function.call(structArray);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        buffer.rewind();        
    }

    @Override
    public int getSize() {
        return structArray.size();
    }

    @Override
    public void index(int index, CallBackFunction<T> function) {
        T t = structArray.get(index);
        function.call(t);
        structArray.set(t, index);
    }

    @Override
    public void set(int index, T t) {
        structArray.set(t, index);
    }

    @Override
    public T get(int index) {
        return structArray.get(index);
    }    
}
