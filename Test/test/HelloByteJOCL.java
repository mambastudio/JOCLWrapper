/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import coordinate.generic.AbstractCoordinate;
import coordinate.struct.ByteStruct;
import coordinate.struct.StructByteArray;
import coordinate.utility.StructInfo;
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
import wrapper.core.buffer.CByteBuffer;

/**
 *
 * @author user
 */
public class HelloByteJOCL {
    public static void main(String... args)
    {
        CPlatform platform = CConfiguration.getDefault();
        CDevice device = platform.getDeviceCPU();
        CContext context = platform.createContext(device);
        CProgram program = context.createProgram(programSource);
        CCommandQueue queue = context.createCommandQueue(device);
        
        int globalSize = 10;
        
        CByteBuffer cbuffer = CBufferFactory.allocByte("paticles", context, StructInfo.getByteArraySize(Particle.class, globalSize), READ_WRITE);
        
        CKernel vectorAdd = program.createKernel("test");
        vectorAdd.putArgs(cbuffer);        
        queue.put1DRangeKernel(vectorAdd, globalSize, 1);
        
        cbuffer.mapReadBuffer(queue, buffer ->{            
            StructByteArray<Particle> array = new StructByteArray(Particle.class, buffer.array());
            for(Particle particle: array)
                System.out.println(particle);
            
        });
    }
        
    public static class Particle extends ByteStruct
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
            this.refreshGlobalArray();
        }
                
        
        @Override
        public void initFromGlobalArray() {            
            
            ByteBuffer buffer = this.getLocalByteBuffer(ByteOrder.nativeOrder());
            int[] offsets = this.getOffsets();
            int pos = buffer.position();
            
            buffer.position(pos + offsets[0]); 
            position.x  =   buffer.getFloat();
            position.y  =   buffer.getFloat();
            position.z  =   buffer.getFloat();
            
            buffer.position(pos + offsets[1]);
            mass        =  buffer.getFloat();     
        }

        @Override
        public byte[] getArray() {            
            ByteBuffer buffer = this.getEmptyLocalByteBuffer(ByteOrder.nativeOrder());
            int offsets[] = this.getOffsets();
            int pos = buffer.position();
            
            buffer.position(pos + offsets[0]);             
            buffer.putFloat(position.x);
            buffer.putFloat(position.y);
            buffer.putFloat(position.z);
            
            buffer.position(pos + offsets[1]);
            buffer.putFloat(mass);
            
            return buffer.array();
        }
        
        @Override
        public String toString()
        {
            return "mass " +mass+ " position " +position;
        }        

    }
    
    public static class Float4 implements AbstractCoordinate
    {
        public float x, y, z, w;
        
        @Override
        public String toString()
        {
            return "x " +x+ " y " +y+ " z " +z;
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
    
    private static final String programSource =
          
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
        "    particles[gid].position = 3 * gid;" +
       // "    printf(\"%2d\\n\", gid);" +                   
        "}";
}
