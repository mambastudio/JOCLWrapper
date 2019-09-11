/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.buffer.local;

import java.nio.Buffer;
import org.jocl.Pointer;
import org.jocl.cl_mem;
import wrapper.core.CMemory;

/**
 *
 * @author user
 */
public class CLocalBuffer extends CMemory
{    
    public CLocalBuffer(long cl_size)   {
        this(null, null, null, cl_size);
    }
    
    private CLocalBuffer(cl_mem memory, Buffer buffer, Pointer pointer, long cl_size) {
        super(memory, buffer, pointer, cl_size);
    }    
}
