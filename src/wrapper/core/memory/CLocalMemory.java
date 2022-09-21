/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory;

import coordinate.struct.Struct;
import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CMemory;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 */
public class CLocalMemory extends CMemory
{    
    public static CLocalMemory LOCALINT     =   new CLocalMemory(Sizeof.cl_int);
    public static CLocalMemory LOCALFLOAT   =   new CLocalMemory(Sizeof.cl_float);
    
    public CLocalMemory(long cl_size)   {
        this(null, null, null, cl_size);
    }
    
    private CLocalMemory(CCommandQueue queue, cl_mem memory, Class clazz, Buffer buffer, Pointer pointer, long cl_size) {
        super(queue, memory, clazz, buffer, pointer, cl_size);
    }
    
    private CLocalMemory(cl_mem memory, Buffer buffer, Pointer pointer, long cl_size) {
        super(null, memory, null, buffer, pointer, cl_size);
    }    

    @Override
    public void setCL(Struct t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Struct getCL() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void index(int index, CallBackFunction function) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(int index, Struct t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Struct get(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
