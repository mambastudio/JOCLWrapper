/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import wrapper.core.CBufferFactory;
import wrapper.core.CCommandQueue;
import wrapper.core.CConfiguration;
import wrapper.core.CContext;
import wrapper.core.CDevice;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CPlatform;
import wrapper.core.CProgram;
import wrapper.core.svm.CSVMFloatBuffer;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class HelloSVM {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDefaultDevice();        
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(CLFileReader.readFile("C:\\Users\\user\\Documents\\Java\\jocl\\cl", "HelloCL.cl"));        
        CCommandQueue queue = context.createCommandQueue(device);
        
        int globalMemory = 10;
        
        CSVMFloatBuffer aBuffer = CBufferFactory.allocSVMFloat(context, globalMemory, READ_WRITE);
        CSVMFloatBuffer bBuffer = CBufferFactory.allocSVMFloat(context, globalMemory, READ_WRITE);
        CSVMFloatBuffer cBuffer = CBufferFactory.allocSVMFloat(context, globalMemory, READ_WRITE);
        
         //create, add variables
        CKernel kernel = program.createSVMKernel("VectorAdd", aBuffer, bBuffer, cBuffer);
       
        // write buffer
        aBuffer.mapWriteBuffer(queue, buffer-> {
            while(buffer.remaining() != 0)
                buffer.put((float)buffer.position());
        });        
        bBuffer.mapWriteBuffer(queue, buffer-> {
            while(buffer.remaining() != 0)
                buffer.put((float)buffer.position());
        });
        
        //Execute kernel
        queue.put1DRangeKernel(kernel, globalMemory, globalMemory);
                
        // read buffer
        cBuffer.mapReadBuffer(queue, buffer->{
            while(buffer.remaining() != 0)
                System.out.println(buffer.get());      
        });
        
    }    
}
