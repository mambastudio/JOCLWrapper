/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory;

import coordinate.struct.structint.IntStruct;
import coordinate.struct.structint.StructIntArray;
import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CMemory;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 * @param <T>
 */
public class IntStructMemory<T extends IntStruct> extends CMemory<T> {
    StructIntArray<T> array;
    
    public IntStructMemory(CCommandQueue queue, cl_mem memory, Class<T> clazz, StructIntArray array, Buffer buffer, Pointer pointer, long cl_size) {
        super(queue, memory, clazz, buffer, pointer, cl_size);
        this.array = array;
    }

    @Override
    public void mapReadIterator(CallBackFunction<Iterable<T>> function) {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        function.call(array);
    }

    @Override
    public void setCL(T t) {
        buffer.clear(); //reset buffer
        array.set(t, 0);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        buffer.rewind(); 
    }

    @Override
    public void mapWriteIterator(CallBackFunction<Iterable<T>> function) {
        buffer.clear(); //reset buffer
        function.call(array);
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
        return array.get(0);
    }

    @Override
    public int getSize() {
        return array.size();
    }

    @Override
    public void index(int index, CallBackFunction<T> function) {
        T t = array.get(index);
        function.call(t);
        array.set(t, index);
    }

    @Override
    public void set(int index, T t) {
        array.set(t, index);
    }

    @Override
    public T get(int index) {
        return array.get(index);
    }
}
