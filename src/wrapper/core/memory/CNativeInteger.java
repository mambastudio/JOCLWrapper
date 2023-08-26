/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory;

import coordinate.memory.NativeInteger;
import coordinate.struct.structint.IntStruct;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CMemory;
import wrapper.core.CallBackFunction;

/**
 *
 * @author jmburu
 * @param <T>
 */
public class CNativeInteger<T extends IntStruct> extends CMemory<T> {

    NativeInteger array;
    
    public CNativeInteger(CCommandQueue queue, cl_mem memory, Class<T> clazz, NativeInteger array, long cl_size) {
        super(queue, memory, clazz, null, Pointer.to(array.getDirectByteBufferPoint()), cl_size);
        this.array = array;
    }
    
    @Override
    public void setCL(T t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T getCL() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(int index, T t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void index(int index, CallBackFunction<T> function) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
