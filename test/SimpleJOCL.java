/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import test.HelloStructType.Float4;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.CMemory;
import test.HelloStructType.Particle;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;

/**
 *
 * @author user
 */
public class SimpleJOCL {
    public static void main(String... args)
    {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(programSource);
                
        //init memory
        CMemory<Particle> particle =  configuration.createBufferB(Particle.class, 1, READ_WRITE);
        particle.setCL(new Particle(99, new Float4(), new Float4()));
        
        //kernel
        CKernel kernel = configuration.createKernel("test", particle);
        
        //execute kernel
        configuration.execute1DKernel(kernel, 1, 1);
        
        //read value
        System.out.println(particle.getCL());
        
    }
    
    private static final String programSource
            = // Definition of the Particle struct in the kernel
            "typedef struct" + "\n"
            + "{" + "\n"
            + "    float4 position;" + "\n"
            + "    float mass;" + "\n"
            + "    float4 velocity;" + "\n"
            + "} Particle;" + "\n"
            + // The actual kernel, performing some dummy computation
            "__kernel void test(__global Particle *particles)" + "\n"
            + "{" + "\n"
            + "    int gid = get_global_id(0);" + "\n"
            + "    particles[gid].mass     += 1;" + "\n"
            + "    particles[gid].position  = gid;" + "\n"
            + "    particles[gid].velocity  = gid;" + "\n"
            + "}";
}
