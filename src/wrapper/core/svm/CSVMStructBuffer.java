/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.svm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jocl.Pointer;
import org.jocl.struct.Buffers;
import org.jocl.struct.Struct;
import wrapper.core.CCommandQueue;
import wrapper.core.CSVMBuffer;
import wrapper.core.CallBackArray;

/**
 *
 * @author user
 * @param <B>
 */
public class CSVMStructBuffer <B extends Struct> extends CSVMBuffer
{    
    private final B[] structs;
    
    public CSVMStructBuffer(B[]structs, Pointer pointer, long cl_size) {
        super(pointer, cl_size);  
        this.structs = structs;
    }
    
    public void readNativeBuffer()
    {
        ByteBuffer structBuffer = pointer.getByteBuffer(0, getCLSize()).order(ByteOrder.nativeOrder()); 
        structBuffer.rewind();
        Buffers.readFromBuffer(structBuffer, structs);
    }
    
    @Override
    public void readNativeBuffer(CCommandQueue queue)
    {        
        queue.mapSVM(this);
        readNativeBuffer();        
        queue.unmapSVM(this);
    }
    
    public B[] mapReadBuffer(CCommandQueue queue, CallBackArray<B> function)
    {
        readNativeBuffer(queue);
        function.call(structs);
        return structs;
    }
       
    public void writeNativeBuffer()
    {
        ByteBuffer structBuffer = pointer.getByteBuffer(0, getCLSize()).order(ByteOrder.nativeOrder()); 
        structBuffer.rewind();
        Buffers.writeToBuffer(structBuffer, structs);
    }
    
    @Override
    public void writeNativeBuffer(CCommandQueue queue)
    {        
        queue.mapSVM(this);
        writeNativeBuffer();        
        queue.unmapSVM(this);
    }
    
    public B[] mapWriteBuffer(CCommandQueue queue, CallBackArray<B> function)
    {        
        function.call(structs);
        writeNativeBuffer(queue);
        return structs;
    }
    
    public B get(int index)
    {
        return structs[index];
    }
    
    public void set(int index, B b)
    {
        structs[index] = b;
    }
   
    public void put(B... bs)
    {
        System.arraycopy(bs, 0, structs, 0, structs.length);            
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for(B b : structs)
            builder.append(b).append("\b").append("\n");
        return builder.toString();
    }

    @Override
    public int getArraySize() {
        return structs.length;
    }
}
