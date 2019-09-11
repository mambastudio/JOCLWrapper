/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import org.jocl.struct.CLTypes;
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
import wrapper.core.CResourceFactory;
import wrapper.core.buffer.CStructBuffer;

/**
 *
 * @author user
 */
public class HelloStruct {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDefaultDevice();        
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(programSource);        
        CCommandQueue queue = context.createCommandQueue(device);
        
        // size
        int n = 10;
                
        //write input
        CStructBuffer<Particle> particleBuffer = CBufferFactory.allocStruct("particles", context, Particle.class, n, READ_WRITE);
        particleBuffer.mapWriteBuffer(queue, particles -> {
            for (int i=0; i<n; i++)
            {
               // particles[i] = new Particle(); //instantiated from allocStruct method above
                particles[i].mass = i;
                particles[i].position.set(0, i);
                particles[i].position.set(1, i);
                particles[i].position.set(2, i);
                particles[i].position.set(3, i);
                particles[i].velocity.set(0, i);
                particles[i].velocity.set(1, i);
                particles[i].velocity.set(2, i);
                particles[i].velocity.set(3, i);
            }
            System.out.println(particleBuffer);
        });
        
        //execute kernel
        CKernel kernel = program.createKernel("test");
        kernel.putArgs(particleBuffer);        
        queue.put1DRangeKernel(kernel, n, 1); 
        
        //read output
        particleBuffer.mapReadBuffer(queue, particle-> {
            System.out.println(particleBuffer);
        });        
        
        CResourceFactory.releaseAll();
    }
    
    public static class Particle extends Struct
    {
        public float mass;
        public CLTypes.cl_float4 position;
        public CLTypes.cl_float4 velocity;
        
        
        @Override
        public String toString()
        {
            return "Particle[" +
            		"mass="+mass+"," +
            		"position="+position+"," +
            		"velocity="+velocity+"]";
        }
    }
    
    /**
     * The source code of the kernel
     */
    private static final String programSource =
        
        // Definition of the Particle struct in the kernel
        "typedef struct" + "\n" +
        "{" + "\n" +
        "    float mass;" + "\n" +
        "    float4 position;" + "\n" +
        "    float4 velocity;" + "\n" +       
        "} Particle;"+ "\n" +

        // The actual kernel, performing some dummy computation
        "__kernel void test(__global Particle *particles)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    particles[gid].mass *= 2;"+ "\n" +
        "    particles[gid].position *= 2;"+ "\n" +
        "    particles[gid].velocity *= 2;"+ "\n" +
        "}";
    
    
}
