/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import coordinate.generic.AbstractCoordinateFloat;
import coordinate.struct.annotation.arraysize;
import coordinate.struct.structbyte.Structure;
import java.util.Arrays;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.CMemory;
import wrapper.core.CKernel;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class SimpleJOCL {
    public static void main(String... args)
    {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "SimpleJOCL.cl"));
                
        //init memory
        
        CMemory<Particle> particle =  configuration.createBufferB(Particle.class, 1, READ_WRITE); 
        
        Particle part = new Particle();
        part.setMass(80);
        part.setCharge(20);
        //set to opencl memory
        particle.setCL(part);
        
        //kernel
        CKernel kernel = configuration.createKernel("test", particle);
        
        //execute kernel
        configuration.execute1DKernel(kernel, 1, 1);
        
        particle.transferFromDevice();
        part.refreshFieldValues();
        
        //read value
        System.out.println(part);
        System.out.println(Arrays.toString(part.binary));
        
    }
    
    public static class Particle extends Structure {
        public float mass;
        public Float4 position;    
        public Atom atom;
        @arraysize(3)
        public int[] binary;
        public Float4 velocity;
        
        
        public void setCharge(int charge) {
            this.atom.charge = charge;
            this.refreshGlobalArray();
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
                    + "velocity = " + velocity + ", "
                    + "atom charge = " + atom.charge + "]";
        }

    }
    
    public static class Atom extends Structure
    {
        public int charge;
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
}
