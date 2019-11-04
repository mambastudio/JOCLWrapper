/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.matrix;

import wrapper.core.CBufferFactory;
import wrapper.core.CCommandQueue;
import wrapper.core.CConfiguration;
import wrapper.core.CContext;
import wrapper.core.CDevice;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.WRITE_ONLY;
import wrapper.core.CPlatform;
import wrapper.core.CProgram;
import wrapper.core.CResourceFactory;
import wrapper.core.buffer.CFloatBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class Test {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDefaultDevice();        
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(CLFileReader.readFile(Test.class, "Matrix4f.cl", "Print.cl", "Kernel.cl"));
        CCommandQueue queue = context.createCommandQueue(device);
        
        int globalSize = 1;
        
        CFloatBuffer aMatrixBuffer = CBufferFactory.allocFloat("matrixA", context, 16, READ_ONLY);
        CFloatBuffer bMatrixBuffer = CBufferFactory.allocFloat("matrixB", context, 16, READ_ONLY);
        CFloatBuffer cMatrixBuffer = CBufferFactory.allocFloat("matrixC", context, 16, WRITE_ONLY);  
        
        aMatrixBuffer.mapWriteBuffer(queue, buffer -> {
            float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 5, 0,
                0, 0, 0, 1
            };
            buffer.put(matrix);
        });     
        
        CKernel vectorAdd = program.createKernel("execute");
        vectorAdd.putArgs(aMatrixBuffer, bMatrixBuffer, cMatrixBuffer);
        
        queue.put1DRangeKernel(vectorAdd, globalSize, globalSize);        
               
        CResourceFactory.releaseAll();
    }
}
