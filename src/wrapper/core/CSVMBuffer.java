/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.Buffer;
import org.jocl.Pointer;

/**
 *
 * @author user
 * @param <B>
 */
public abstract class CSVMBuffer<B extends Buffer> extends CObject implements CResource
{    
    protected long cl_size;
    protected Pointer pointer;
    
    public CSVMBuffer(Pointer pointer, long cl_size) {
        super(null);
        this.pointer = pointer;
        this.cl_size = cl_size;
        
    }
    
    public abstract int getArraySize();
    
    public long getCLSize()
    {
        return cl_size;
    }
    
    @Override
    public void release()
    {
        //svm has no cl_mem... there are exceptional situations when it is required
        //check intel svm documentation for more information.
    }
    
    public Pointer getSVMPointer()
    {
        return pointer;
    }
    
    public abstract void writeNativeBuffer(CCommandQueue queue);
    public abstract void readNativeBuffer(CCommandQueue queue);
       
}
