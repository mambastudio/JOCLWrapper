/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.svm;

import coordinate.memory.type.LayoutArray;
import coordinate.memory.type.LayoutMemory;
import coordinate.memory.type.MemoryAllocator;
import coordinate.memory.type.MemoryRegion;
import coordinate.memory.type.StructBase;
import coordinate.utility.RangeCheck;
import coordinate.utility.RangeCheckArray;
import coordinate.utility.RangeLong;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jocl.CL.CL_MAP_READ;
import static org.jocl.CL.CL_MAP_WRITE;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_SVM_FINE_GRAIN_BUFFER;
import static org.jocl.CL.clEnqueueSVMFree;
import static org.jocl.CL.clEnqueueSVMMap;
import static org.jocl.CL.clEnqueueSVMUnmap;
import static org.jocl.CL.clSVMAlloc;
import org.jocl.Pointer;
import org.jocl.SVMFreeFunction;
import org.jocl.cl_command_queue;
import wrapper.core.CCommandQueue;
import wrapper.core.CContext;
import wrapper.core.CMemorySkeleton;
import static wrapper.core.svm.SVMNative.MemStrategy.COARSE_GRAIN;
import static wrapper.core.svm.SVMNative.MemStrategy.FINE_GRAIN;

/**
 *
 * @author user
 * @param <T>
 * 
 * Rules to observe:
 *  1. Always maintain a record of the original size and pointer when creating sub memory
 *      i. Since enqueueMap and enqueueUnmap methods require them for coarse_grain memory read and write
 *  2. Once COARSE_GRAIN or FINE_GRAIN is created, they are final and cannot be changed dynamically
 * 
 * 
 */
public class SVMNative<T extends StructBase> implements CMemorySkeleton<T, SVMNative<T>> {
    
    public enum MemStrategy{COARSE_GRAIN, FINE_GRAIN};
    
    private MemStrategy memStrategy = COARSE_GRAIN;
    
    private MemoryRegion memory;    
         
    private final T structBase;
    private final int maximumStringCount = 3000;
    
    private final CCommandQueue queue;
    private final CContext context;
    
    //the roots variables are for mapping and unmapping
    private Pointer svm;
    private Pointer svmRoot;
    private long size;   
    private long sizeRoot;
    
    private boolean isSubMemory;
    
    private boolean mapped = false;
    
    private boolean isFree = false;
    
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
    
    public SVMNative(CCommandQueue queue, CContext context, MemStrategy memStrategy, T t, long size)
    {
        Objects.requireNonNull(t);
        RangeCheckArray.validateIndexSize(size, Long.MAX_VALUE);        
        this.structBase = t;
        this.size = size;
        this.sizeRoot = size;
        
        this.memStrategy = memStrategy;
        long memType;
        if(memStrategy == FINE_GRAIN)
            memType = CL_MEM_READ_WRITE | CL_MEM_SVM_FINE_GRAIN_BUFFER;
        else
            memType = CL_MEM_READ_WRITE;
        
        this.queue = queue;
        this.context = context;
        LayoutMemory memoryLayout = LayoutArray.arrayLayout(size, t.getLayout());
        svm = clSVMAlloc(context.getId(), 
            memType, memoryLayout.byteSizeAggregate(), 0); 
        svmRoot = svm;
        this.memory = MemoryAllocator.allocateNativeAddress(t.sizeOf() * size, getAddressFromPointer());
        this.isSubMemory = false;        
    }
    
    public SVMNative(CCommandQueue queue, CContext context, T t, long size)
    {
        this(queue, context, COARSE_GRAIN, t, size);
    }
    
    private SVMNative(SVMNative<T> parent, long offsetIndex)
    {
        RangeCheck.rangeNotNegative(offsetIndex);
        
        this.memStrategy = parent.memStrategy;
        
        this.queue = parent.queue;
        this.context = parent.context;
        this.isSubMemory = true;
        this.structBase = (T) parent.getStructBase().copyStruct();
        
        this.svm = parent.svm.withByteOffset(parent.getOffsetByte(offsetIndex));
        this.svmRoot = parent.svmRoot;
        
        this.memory = parent.memory.offsetIndex(offsetIndex, parent.getStructBase().getLayout());
       
        this.size = parent.size() - offsetIndex;   
        this.sizeRoot = parent.sizeRoot;
    }
    
    public SVMNative(T t)
    {
        Objects.requireNonNull(t);
        RangeCheckArray.validateIndexSize(1, Long.MAX_VALUE);
                
        this.queue = null;
        this.context = null;
        isSubMemory = false;
        this.structBase = t;
        
        svm = null;
        svmRoot = null;
        
        this.memory = null;
        
        this.size = 1;    
        this.sizeRoot = 1;   
    }
    
    @Override
    public boolean isSVM() {
        return true;
    }
    
    public boolean isFineGrain()
    {
        return memStrategy == FINE_GRAIN;
    }
    
    public boolean isCoarseGrain()
    {
        return memStrategy == COARSE_GRAIN;
    }

    @Override
    public Pointer getPointer() {
        return svm;
    }

    @Override
    public boolean isSubMemory() {
        return isSubMemory;
    }

    @Override
    public void reallocate(long size) {
        if(isSubMemory)
            throw new UnsupportedOperationException("cannot reallocate since it's a sub memory");
        free();
        RangeCheckArray.validateIndexSize(size, Long.MAX_VALUE);   
        
        long memType;
        if(memStrategy == FINE_GRAIN)
            memType = CL_MEM_READ_WRITE | CL_MEM_SVM_FINE_GRAIN_BUFFER;
        else
            memType = CL_MEM_READ_WRITE;        
        this.size = size;
        this.sizeRoot = size;                
        
        LayoutMemory memoryLayout = LayoutArray.arrayLayout(size, getStructBase().getLayout());
        svm = clSVMAlloc(context.getId(), 
            memType, memoryLayout.byteSizeAggregate(), 0); 
        svmRoot = svm;
        
        this.memory = MemoryAllocator.allocateNativeAddress(getStructBase().sizeOf() * size, getAddressFromPointer());
        this.size = size;            
    }
    
    public void forEach(LongConsumer consumer)
    {
        mapWriteFunction(()->{
            for(long i = 0; i<size(); i++)
                consumer.accept(i);
            return Optional.empty();
        });
    }
    
    public void apply(int index, Function<T, T> function)
    {
        T getT = get(index);
        mapWriteFunction(()->{
            T t = function.apply(getT);
            setStraight(index, t);
            return Optional.of(t);
        });
    }
    
    public void apply(Function<T, T> function)
    {
        apply(0, function);
    }
    
    @Override
    public void set(T t) {
        set(0, t);
    }

    @Override
    public void set(long index, T t) {
        mapWriteFunction(()->{
            setStraight(index, t);  
            return Optional.empty();
        });
    }
    
    public void set(Consumer<MemoryRegion> consume)
    {
        mapWriteFunction(()->{
            consume.accept(memory);
            return Optional.empty();
        });        
    }
    
    public void setAll(T t)
    {
        mapWriteFunction(()->{
            forEach(index ->{
                setStraight(index, t);      
            });
            return Optional.empty();
        });
    }
    
    public void setStraight(long index, T t) {
        if(!mapped && isCoarseGrain())
            throw new UnsupportedOperationException("shared virtual memory is coarse grain but this call not mapped");
        t.fieldToMemory(memory.offsetIndexSegment(index, getStructBase().getLayout()));  
    }
    
    public void setStraight(T t){
        setStraight(0, t);
    }
    
    public void setStraightAll(T t) {
        forEach(index ->{
            setStraight(index, t);      
        });
    }

    @Override
    public T get(long index) {
        T t = mapReadFunction(()->{
            T newT = getStraight(index);  
            return Optional.of(newT);
        });
        return t;
    }    
    
    @Override
    public T get() {
        return get(0);
    }
    
    public void obtain(Consumer<MemoryRegion> consume)
    {
        mapReadFunction(()->{
            consume.accept(memory);
            return Optional.empty();
        });    
    }
    
    
    public T getStraight(long index)
    {
        if(!mapped && isCoarseGrain())
            throw new UnsupportedOperationException("shared virtual memory is coarse grain but this call not mapped");
        T newT = (T) structBase.newStruct();
        newT.memoryToField(memory.offsetIndexSegment(index, getStructBase().getLayout()));  
        return newT;
    }

    public T getStraight()
    {
        return getStraight(0);
    }

    @Override
    public String getString(RangeLong range, Function<T, ?> function) {        
        RangeCheckArray.validateRangeSize(range.low(), range.high(), size());     
        StringJoiner joiner = new StringJoiner(", ", "[", "]");      
        mapReadFunction(()->{                    
            for(long i = range.low(); i < range.high(); i++)
                joiner.add(""+function.apply(getStraight(i)));
            return Optional.empty();
        });
        return joiner.toString();
    }

    @Override
    public String getString(RangeLong range) {
        RangeCheckArray.validateRangeSize(range.low(), range.high(), size());
        StringJoiner joiner = new StringJoiner(", ", "[", "]");       
        mapReadFunction(()->{
            for(long i = range.low(); i < range.high(); i++)
                joiner.add(""+getStraight(i)); 
            return Optional.empty();
        });
        return joiner.toString();
    }
    
    @Override
    public String toString()
    {
        return getString(new RangeLong(0, Math.min(size(), maximumStringCount)));
    }

    @Override
    public SVMNative<T> offsetIndex(long offsetIndex) {          
        return new SVMNative(this, offsetIndex);
    }

    //native copy (cpu)
    @Override
    public void copyFrom(SVMNative<T> m) {
        mapWriteFunction(()->{
            MemoryRegion mTemp = m.memory;
            memory.copyFrom(mTemp, mTemp.byteCapacity());
            return Optional.ofNullable(null);
        });
    }

    //native copy (cpu)
    @Override
    public void copyTo(SVMNative<T> m) {
        mapWriteFunction(()->{
            MemoryRegion mTemp = m.memory;
            memory.copyTo(mTemp, mTemp.byteCapacity());
            return Optional.ofNullable(null);
        });
    }

    //native copy (cpu)
    @Override
    public SVMNative<T> copy() {
        SVMNative<T> mem = new SVMNative(queue, context, memStrategy, getStructBase(), size);
        copyTo(mem);
        return mem;
    }

    @Override
    public void swapElement(long index1, long index2) {        
        RangeCheckArray.validateIndexSize(index1, size());
        RangeCheckArray.validateIndexSize(index2, size());        
        T temp = get(index1);
        set(index1, get(index2));
        set(index2, temp);        
    }

    @Override
    public long address() {
        return memory.address();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public long getByteCapacity() {
        return memory.byteCapacity();
    }
    
    public void swap(SVMNative<T> memStruct)
    {        
        MemStrategy tempMemStrategy = memStrategy;
        boolean tempIsSubMemory     = isSubMemory;
        Pointer tempSvm             = svm;
        Pointer tempSvmRoot         = svmRoot;
        MemoryRegion tempMemory     = memory;
        long tempSize               = size;
        long tempSizeRoot           = sizeRoot;
        
        memStrategy     = memStruct.memStrategy;
        isSubMemory     = memStruct.isSubMemory;
        svm             = memStruct.svm;
        svmRoot         = memStruct.svmRoot;
        memory          = memStruct.memory;
        size            = memStruct.size;
        sizeRoot        = memStruct.sizeRoot;
        
        memStruct.memStrategy   = tempMemStrategy;
        memStruct.isSubMemory   = tempIsSubMemory;
        memStruct.svm           = tempSvm;
        memStruct.svmRoot       = tempSvmRoot;
        memStruct.memory        = tempMemory;
        memStruct.size          = tempSize;
        memStruct.sizeRoot      = tempSizeRoot;
    }

    @Override
    public void free() {
        if(isSubMemory || isFree()) //should free from the main memory
            return;
        SVMFreeFunction callback = (cl_command_queue que, int num_svm_pointers, Pointer[] svm_pointers, Object user_data) -> {
         //   memory.dispose(); - no need, since the pointer is released by opencl through clEnqueueSVMFree
        };
        clEnqueueSVMFree(queue.getId(), 1, new Pointer[]{svm}, 
            callback, null, 0, null, null);   
        isFree = true;
    }
    
    @Override
    public boolean isFree()
    {
        return isFree;
    }

    @Override
    public MemoryRegion getMemory() {
        return memory;
    }

    @Override
    public T getStructBase() {
        return structBase;
    }
    
    public void enqueueRead()
    {        
        mapped = true;
        clEnqueueSVMMap(queue.getId(), true, CL_MAP_READ, 
                svmRoot, byteCapacityRoot(), 0, null, null); 
    }
    
    public void enqueueWrite()
    {        
        mapped = true;
        clEnqueueSVMMap(queue.getId(), true, CL_MAP_WRITE,
                svmRoot, byteCapacityRoot(), 0, null, null);
    }
    
    public void enqueueUnmap()
    {
        mapped = false;
        clEnqueueSVMUnmap(queue.getId(), svmRoot, 0, null, null);
    }
    
    private T mapReadFunction(Supplier<Optional<T>> callFunction)
    {
        if(isCoarseGrain())
            enqueueRead();
        Optional<T> option = callFunction.get();
        if(isCoarseGrain())
            enqueueUnmap();
        return option.orElse(null);
    }
    
    private void mapWriteFunction(Supplier<Optional<T>> callFunction)
    {
        if(isCoarseGrain())
            enqueueWrite();
        callFunction.get();
        if(isCoarseGrain())
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
    
    public long byteCapacityRoot()
    {
        return sizeRoot * structBase.sizeOf();
    }
}
