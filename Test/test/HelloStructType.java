/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import coordinate.generic.AbstractCoordinate;
import coordinate.struct.ByteStruct;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import wrapper.core.buffer.CStructTypeBuffer;

/**
 *
 * @author user
 */
public class HelloStructType {

    public static void main(String... args) {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDeviceGPU();
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(programSource);
        CCommandQueue queue = context.createCommandQueue(device);

        // size
        int n = 13;

        //write input
        CStructTypeBuffer<Particle> particleBuffer = CBufferFactory.allocStructType("particle", context, Particle.class, n, READ_WRITE);

        particleBuffer.mapWriteBuffer(queue, particles -> {
            for (Particle particle : particles) {
                particle.setPosition(1, 4, 3);
            }
        });

        //execute kernel
        CKernel kernel = program.createKernel("test");
        kernel.putArgs(particleBuffer);
        queue.put1DRangeKernel(kernel, n, 1);

        //read output
        particleBuffer.mapReadBuffer(queue, particles -> {
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
        public void initFromGlobalArray() {

            ByteBuffer buffer = this.getLocalByteBuffer(ByteOrder.nativeOrder()); //main buffer but position set to index and limit to size of struct
            int[] offsets = this.getOffsets();
            int pos = buffer.position();

            buffer.position(pos + offsets[0]);
            position.x = buffer.getFloat();
            position.y = buffer.getFloat();
            position.z = buffer.getFloat();

            buffer.position(pos + offsets[1]);
            mass = buffer.getFloat();

            buffer.position(pos + offsets[2]);
            velocity.x = buffer.getFloat();
            velocity.y = buffer.getFloat();
            velocity.z = buffer.getFloat();
        }

        @Override
        public byte[] getArray() {

            ByteBuffer buffer = this.getEmptyLocalByteBuffer(ByteOrder.nativeOrder());
            int[] offsets = this.getOffsets();
            int pos = buffer.position();

            buffer.position(pos + offsets[0]);
            buffer.putFloat(position.x);
            buffer.putFloat(position.y);
            buffer.putFloat(position.z);
            buffer.putFloat(position.w);

            buffer.position(pos + offsets[1]);
            buffer.putFloat(mass);

            buffer.position(pos + offsets[2]);
            buffer.putFloat(velocity.x);
            buffer.putFloat(velocity.y);
            buffer.putFloat(velocity.z);
            buffer.putFloat(velocity.w);

            return buffer.array();
        }

        @Override
        public String toString() {
            return "Particle["
                    + "mass = " + mass + ", "
                    + "position = " + position + ", "
                    + "velocity = " + velocity + "]";
        }

    }

    public static class Float4 implements AbstractCoordinate {

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
