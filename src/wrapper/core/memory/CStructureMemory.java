/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory;

import coordinate.struct.StructAbstractCache;
import coordinate.struct.StructAbstractMemory;
import wrapper.core.CMemory;
import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CCommandQueue;
import wrapper.core.CallBackFunction;

/**
 *
 * @author user
 * @param <MemoryType>
 * @param <GlobalBuffer>
 */
public class CStructureMemory<MemoryType extends StructAbstractMemory<GlobalBuffer>, GlobalBuffer> extends CMemory<MemoryType>  {
    private final StructAbstractCache<MemoryType, GlobalBuffer> structArray;
    private final long flag;
    
    public CStructureMemory(CCommandQueue queue, cl_mem memory, Class<MemoryType> clazz, StructAbstractCache<MemoryType, GlobalBuffer> structArray, Buffer buffer, Pointer pointer, long cl_size, long flag) {
        super(queue, memory, clazz, buffer, pointer, cl_size);
        this.structArray = structArray;
        this.flag = flag;
        
    }
    
    public boolean isUseHostPointer()
    {
        return flag == USE_HOST_PTR;
    }
    
    @Override
    public void setCL(MemoryType t)
    {
        buffer.clear(); //reset buffer
        structArray.setStruct(t, 0);
        buffer.rewind();  // set read position to 0 but limit remains same
        queue.putWriteBuffer(this);
        buffer.rewind(); 
    }
    
    
    @Override
    public MemoryType getCL() {
        buffer.clear(); //reset buffer to 0 position
        buffer.rewind();
        queue.putReadBuffer(this);
        buffer.rewind(); //set read position to 0 but limit remain same
        return structArray.get(0);
    }

    

    @Override
    public long getSize() {
        return structArray.size();
    }

    @Override
    public void index(int index, CallBackFunction<MemoryType> function) {
        MemoryType t = structArray.get(index);
        function.call(t);
        structArray.setStruct(t, index);
    }

    @Override
    public void set(int index, MemoryType t) {
        structArray.setStruct(t, index);
    }

    @Override
    public MemoryType get(int index) {
        return structArray.get(index);
    }    
}
