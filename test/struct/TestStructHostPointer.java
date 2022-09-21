/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package struct;

import coordinate.generic.AbstractCoordinateFloat;
import coordinate.struct.cache.StructDirectCache;
import coordinate.struct.structbyte.StructBufferMemory;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import static wrapper.core.CMemory.USE_HOST_PTR;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class TestStructHostPointer {
    public static void main(String... args) {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(programSource);

        // size
        int n = 6;

        //write input
        CMemory<Particle> particleBuffer = configuration.createBufferB(Particle.class, StructDirectCache.class, n, READ_WRITE|USE_HOST_PTR);

        particleBuffer.loopWrite((particle, index)->{            
            particle.setPosition(1, 4, 3);
        });
        
        particleBuffer.transferToDevice();

        //execute kernel
        CKernel kernel = configuration.createKernel("test", particleBuffer);     
        configuration.execute1DKernel(kernel, n, 1);        
        particleBuffer.transferFromDevice();
        
        //read output
        particleBuffer.loopRead((particle, index)->{
            System.out.println(particle);
        });       
        
        CResourceFactory.releaseAll();
    }

    public static class Particle extends StructBufferMemory {

        public Float4 position;
        public float mass;
        public Float4 velocity;

        public void setMass(float mass) {
            this.mass = mass;
            this.refreshGlobalBuffer();
        }

        public void setPosition(float x, float y, float z) {
            position.x = x;
            position.y = y;
            position.z = z;
            this.refreshGlobalBuffer();
        }

        public void setVelocity(float x, float y, float z) {
            velocity.x = x;
            velocity.y = y;
            velocity.z = z;
            this.refreshGlobalBuffer();
        }
       
        @Override
        public String toString() {
            return "Particle["
                    + "mass = " + mass + ", "
                    + "position = " + position + ", "
                    + "velocity = " + velocity + "]";
        }

    }

    public static class Float4 implements AbstractCoordinateFloat {

        public float x, y, z, w;

        @Override
        public String toString() {
            return "x " + x + " y " + y + " z " + z;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public int getByteSize() {
            return 4;
        }

        @Override
        public void set(float... values) {
            x = values[0];
            y = values[1];
            z = values[2];
            w = values[3];
        }

        @Override
        public float[] getArray() {
            return new float[]{x, y, z, w};
        }
    }

    private static String programSource
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
            + "    particles[gid].mass     = 56;" + "\n"
            + "    particles[gid].position  *= gid;" + "\n"
            + "    particles[gid].velocity  = gid;" + "\n"
            + "}";
}
