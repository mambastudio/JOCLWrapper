/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package struct;

import coordinate.struct.cache.StructBufferCache;
import coordinate.struct.structbyte.StructBufferMemory;
import org.jocl.CL;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestCastStruct {
    public static void main(String... args) {
        
        CL.setExceptionsEnabled(true);
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CLFileReader.readFile(TestStruct.class, "StructTestCasting.cl"));
        
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(new String[]{stringBuilder.toString()});
        
        CMemory<Entry> simpleStruct = configuration.createBufferB(Entry.class, StructBufferCache.class, 1, READ_WRITE);
        
        simpleStruct.mapWriteMemory(structCL->{
            structCL.set(0, new Entry());
        });
            
        CKernel testStructKernel = configuration.createKernel("testStruct", simpleStruct);
        configuration.execute1DKernel(testStructKernel, 1, 1);       
        
    }
    
    public static class Entry extends StructBufferMemory {
        public int log_dim;    
        public int begin;

        public Entry()
        {
            log_dim = 3;
            begin = 4;
        }
    }

}
