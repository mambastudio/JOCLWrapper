/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import coordinate.generic.AbstractCoordinateFloat;
import coordinate.struct.ByteStruct;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class HelloStructType {

    public static void main(String... args) {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(programSource);

        // size
        int n = 13;

        //write input
        CMemory<Particle> particleBuffer = configuration.createBufferB(Particle.class, n, READ_WRITE);

        particleBuffer.mapWriteIterator(particles -> {
            for (Particle particle : particles) {
                particle.setPosition(1, 4, 3);
            }
        });

        //execute kernel
        CKernel kernel = configuration.createKernel("test", particleBuffer);     
        configuration.execute1DKernel(kernel, n, 1);
        
        
        //read output
        particleBuffer.mapReadIterator(particles -> {
            for (Particle particle : particles) {
                System.out.println(particle);
            }
        });

        CResourceFactory.releaseAll();
    }

    public static class Particle extends ByteStruct {

        public Float4 position;
        public float mass;
        public Float4 velocity;

        public Particle() {
            mass = 0;
            position = new Float4();
            velocity = new Float4();
        }
        
        public Particle(float mass, Float4 position, Float4 velocity) {
            this.mass = mass;
            this.position = position;
            this.velocity = velocity;
        }

        public void setMass(float mass) {
            this.mass = mass;
            this.refreshGlobalArray();
        }

        public void setPosition(float x, float y, float z) {
            position.x = x;
            position.y = y;
            position.z = z;
            this.refreshGlobalArray();
        }

        public void setVelocity(float x, float y, float z) {
            velocity.x = x;
            velocity.y = y;
            velocity.z = z;
            this.refreshGlobalArray();
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
            + "    particles[gid].mass     = gid;" + "\n"
            + "    particles[gid].position  *= gid;" + "\n"
            + "    particles[gid].velocity  = gid;" + "\n"
            + "}";
}