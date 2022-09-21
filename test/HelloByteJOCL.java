/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import coordinate.generic.AbstractCoordinateFloat;
import coordinate.struct.cache.StructArrayCache;
import coordinate.struct.structbyte.StructArrayMemory;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class HelloByteJOCL {
    public static void main(String... args)
    {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(ProgramSource);
        
        int globalSize = 10;
        
        CMemory<Particle> cbuffer = configuration.createBufferB(Particle.class, StructArrayCache.class, globalSize, READ_WRITE);
                
        CKernel vectorAdd = configuration.createKernel("test", cbuffer);
        configuration.execute1DKernel(vectorAdd, globalSize, 1);
        
        cbuffer.loopRead((particle, index)->{
            System.out.println(particle);
        });                
    }
        
    public static class Particle extends StructArrayMemory
    {        
        public Float4 position;
        public float mass;         
                        
        public Particle()
        {
            position = new Float4();
            mass = 0;
        }
       
        public void setMass(float mass)
        {
            this.mass = mass;
            this.refreshGlobalBuffer();
        }
                
        
       
        
        @Override
        public String toString()
        {
            return "mass " +mass ;
        }        

    }
    
    public static class Float4 implements AbstractCoordinateFloat
    {
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
    
    private static final String ProgramSource =
          
        "typedef struct" + "\n" +
        "{" + "\n" +              
        "    float4 position;      "+ "\n"+
        "    float  mass;" + "\n" +              
        "} Particle;"+ "\n" +
            
        // The actual kernel, performing some dummy computation
        "__kernel void test(__global Particle *particles)"+ "\n" +
        "{"+ "\n" +
        "    int gid = get_global_id(0);"+ "\n" +
        "    particles[gid].mass = gid * 2;"+
        "    particles[gid].position = gid * 3;" +
       // "    printf(\"%2d\\n\", gid);" +                   
        "}";
}
