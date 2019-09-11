/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;


import org.jocl.struct.CLTypes.cl_float4;
import org.jocl.struct.Struct;
import wrapper.core.CBufferFactory;
import wrapper.core.CCommandQueue;
import wrapper.core.CConfiguration;
import wrapper.core.CContext;
import wrapper.core.CDevice;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CPlatform;
import wrapper.core.CProgram;
import wrapper.core.svm.CSVMStructBuffer;

/**
 *
 * @author user
 */
public class HelloStructSVM {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDefaultDevice();        
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(programSource);
        CCommandQueue queue = context.createCommandQueue(device);
        
        // Initialization of an array containing some particles
        int n = 10;        
        CSVMStructBuffer<Particle> buffer = CBufferFactory.allocSVMStruct(context, Particle.class, n, READ_WRITE);
        
        // Write buffer
        buffer.mapWriteBuffer(queue, particles->{            
            for(Particle particle : particles)
                particle.mass = 50; 
        });
                
        //create, add variables, execute kernel
        CKernel kernel = program.createKernel("test");
        kernel.putSVMArgs(buffer);
        queue.put1DRangeKernel(kernel, n, n);
        
        // Read buffer
        buffer.mapReadBuffer(queue, particles->{             
            StringBuilder builder = new StringBuilder();
            for(Particle particle : particles)
                builder.append(particle).append("\b").append("\n");
            System.out.println(builder);
        });
       
    }
    
    private static final String programSource =
        
        // Definition of the Particle struct in the kernel
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    float mass;" + "\n" +  
        "    float4 position;" + "\n" +       
        "} Particle;"+ "\n" +

        // The actual kernel, performing some dummy computation
        "__kernel void test(__global Particle *particles)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    particles[gid].mass *= 8;"+ "\n" +   
        "    particles[gid].position = 232001;"+ "\n" +
        "}";
    
    public static class Particle extends Struct
    {
        public float mass;
        public cl_float4 position;        
        
        @Override
        public String toString()
        {
            return "Particle[" +
            		"mass="+mass+ ",   "+
                        "position="+position+"]";
        }
    }
}
