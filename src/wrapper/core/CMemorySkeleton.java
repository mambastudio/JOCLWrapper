/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStruct.BiObjLongFunction;
import coordinate.memory.type.StructBase;
import coordinate.utility.RangeLong;
import java.util.Objects;
import java.util.function.Consumer;
import org.jocl.Pointer;

/**
 *
 * @author jmburu
 * @param <T>
 */
public interface CMemorySkeleton<T extends StructBase> {
    public boolean isSVM();
    public Pointer getPointer();
    public long getByteCapacity();
    public boolean isSubMemory();
    public void free();
    default boolean isLocal()
    {
        return Objects.isNull(getPointer());
    }
    public long size();
    public T getStructBase();
    default long elementSize()
    {
        return getStructBase().sizeOf();
    }
    
    public void write(BiObjLongFunction<T> function);    
    public void write(RangeLong range, BiObjLongFunction<T> function);    
    public void write(T t);    
    public void write(long index, T t);
    public void write(Consumer<MemoryStruct<T>> consume);
    public T read(long index);
    public T read();
    default T readLast(){return read(size()-1);}
}
