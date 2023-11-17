/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.LayoutArray;
import coordinate.memory.type.LayoutMemory;
import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStruct.BiObjLongFunction;
import coordinate.memory.type.StructBase;
import coordinate.utility.RangeLong;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jocl.CL.CL_MAP_READ;
import static org.jocl.CL.CL_MAP_WRITE;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.clEnqueueSVMFree;
import static org.jocl.CL.clEnqueueSVMMap;
import static org.jocl.CL.clEnqueueSVMUnmap;
import static org.jocl.CL.clSVMAlloc;
import org.jocl.Pointer;
import org.jocl.SVMFreeFunction;
import org.jocl.cl_command_queue;

/**
 *
 * @author user
 * @param <T>
 */
public class SVMNative<T extends StructBase> implements CMemorySkeleton<T>{    
    private final CCommandQueue queue;
    private final MemoryStruct<T> memory;
    private final Pointer svm;
    
    private final boolean isSubMemory;
    
    private final T localT; //for local memory
    
    //buffer fields
    private static final Field addressField, capacityField;
    static {
    try {
            addressField = Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            capacityField = Buffer.class.getDeclaredField("capacity");
            capacityField.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
    
    public SVMNative(T t)
    {
        this.localT = t;
        queue = null;
        memory = null;
        svm = null;
        isSubMemory = false;
    }
    
    public SVMNative(CCommandQueue queue, CContext context, T t, long size)
    {       
        this.queue = queue;
        LayoutMemory memoryLayout = LayoutArray.arrayLayout(size, t.getLayout());
        svm = clSVMAlloc(context.getId(), 
            CL_MEM_READ_WRITE, memoryLayout.byteSizeAggregate(), 0);         
        memory = new MemoryStruct(t, getAddressFromPointer(), size);   
        isSubMemory = false;
        this.localT = null;
    }
    
    private SVMNative(CCommandQueue queue, Pointer subSvm, MemoryStruct<T> subMemory)
    {
        this.queue = queue;
        this.svm = subSvm;   
        this.memory = subMemory;
        this.isSubMemory = true;
        localT = null;
    }
    
    @Override
    public void free()
    {
        SVMFreeFunction callback = (cl_command_queue queue, int num_svm_pointers, Pointer[] svm_pointers, Object user_data) -> {
         //   memory.dispose(); - no need, since the pointer is released by opencl through clEnqueueSVMFree
        };
        clEnqueueSVMFree(queue.getId(), 1, new Pointer[]{svm}, 
            callback, null, 0, null, null);
        
    }
    
    public final void fill(byte value) {        
        enqueueWrite();
        memory.getMemory().fill(value);
        enqueueUnmap();        
    }
    
    private long getAddressFromPointer()
    {
        ByteBuffer bb = svm.getByteBuffer(0, 1);
        long address = -1;        
        try {
            address = addressField.getLong(bb);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SVMNative.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    
    private void enqueueRead()
    {
        clEnqueueSVMMap(queue.getId(), true, CL_MAP_READ, 
            svm, memory.getMemory().byteCapacity(), 0, null, null);
    }
    
    private void enqueueWrite()
    {
        clEnqueueSVMMap(queue.getId(), true, CL_MAP_WRITE, 
            svm, memory.getMemory().byteCapacity(), 0, null, null);
    }
    
    private void enqueueUnmap()
    {
        clEnqueueSVMUnmap(queue.getId(), svm, 0, null, null);
    }

    @Override
    public boolean isSVM() {
        return true;
    }

    @Override
    public Pointer getPointer() {
        return svm;
    }

    @Override
    public long getByteCapacity() {
        return memory.getMemory().byteCapacity();
    }
    
    public String getString(RangeLong range, Function<T, ?> function)
    {
        enqueueRead();
        String string = memory.getString(range, function);
        enqueueUnmap();
        return string;
    }
    
    public String getString(RangeLong range)
    {
        enqueueRead();
        String string = memory.getString(range);
        enqueueUnmap();
        return string;
    }
    
    @Override
    public void write(BiObjLongFunction<T> function)
    {
        enqueueWrite();
        memory.forEachSet(function);
        enqueueUnmap();   
    }
    
    @Override
    public void write(RangeLong range, BiObjLongFunction<T> function)
    {
        enqueueWrite();
        memory.forEachSet(range, function);
        enqueueUnmap();
    }
    
    @Override
    public void write(T t)
    {
        enqueueWrite();
        memory.set(0, t);
        enqueueUnmap();
    }
    
    @Override
    public void write(long index, T t)
    {
        enqueueWrite();
        memory.set(index, t);
        enqueueUnmap();
    }
    
    @Override
    public T read(long index)
    {
        enqueueRead();
        T t = memory.get(index);
        enqueueUnmap();
        return t;
    }
    
    @Override
    public T read()
    {
        return read(0);
    }
    
    @Override
    public long size()
    {
        return memory.size();
    }
    
    public SVMNative<T> offsetIndex(long index)
    {
        MemoryStruct<T> subMemory = memory.offsetIndex(index);
        Pointer subSvmPointer = svm.withByteOffset(memory.getOffsetByte(index));
        return new SVMNative(queue, subSvmPointer, subMemory);
    }
    
    @Override
    public String toString()
    {
        return getString(new RangeLong(0, size()));
    }

    @Override
    public boolean isSubMemory() {
        return isSubMemory;
    }

    @Override
    public T getStructBase() {
        if(memory == null)
            return localT;
        else
            return memory.getStructBase();
    }
}
