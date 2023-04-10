/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.image;

import coordinate.struct.cache.StructJNACache;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.memory.values.IntValue;

/**
 *
 * @author user
 */
public class CImage3D_ARGB {
    
    OpenCLConfiguration config;
    int width, height, depth;
    long bufferLength;
    
    CMemory<C_ARGB> buffer = null;
    
    public CImage3D_ARGB(OpenCLConfiguration config, int width, int height, int depth)
    {
        this.width = width; this.height = height; this.depth = depth;
        this.bufferLength = width * 4 * height * 4 * depth * new C_ARGB().sizeof();
        this.config = config;
        this.initBuffer();
    }
                
    public final void initBuffer()
    {
        buffer  = config.createBufferB(C_ARGB.class, StructJNACache.class, bufferLength, READ_WRITE);
    }
    
    public void setImageAtDepth(int index, int[] image)
    {
        if(image == null)
            throw new NullPointerException("image array is null");
        if(image.length != size2D())
            throw new UnsupportedOperationException("array is not the desired size");
        
    }
    
    protected int size2D()
    {
        return width * height;
    }
}
