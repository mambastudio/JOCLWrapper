/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.nio.Buffer;
import org.jocl.NativePointerObject;
import org.jocl.Pointer;

/**
 *
 * @author user
 */
public class CPointer extends NativePointerObject{
    private final long pointer;
    private final Buffer buffer;
    
    public CPointer(long pointer)
    {
        this.buffer = null;
        this.pointer = pointer;
    }
        
    long getByteOffset()
    {
        return 0;
    }
    
    Buffer getBuffer()
    {
        return buffer;
    }
    
    @Override
    protected long getNativePointer()
    {
        return pointer;
    }
    
    public Pointer getPointer()
    {
        return Pointer.to(this);
    }
    
    @Override
    public int hashCode()
    {
        int result = 227;
        int c = (int)(pointer ^ (pointer >>> 32));
        return 37 * result + c;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CPointer other = (CPointer) obj;
        return this.pointer == other.pointer;
    }
}
