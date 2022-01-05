/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package struct;

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
public class TestStruct {
    public static void main(String... args) {
        
        CL.setExceptionsEnabled(true);
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CLFileReader.readFile(TestStruct.class, "TestStruct.cl"));
        
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(new String[]{stringBuilder.toString()});
    
        CMemory<SimpleStruct> simpleStruct = configuration.createBufferB(SimpleStruct.class, 1, READ_WRITE);
        
        simpleStruct.mapReadMemory(structCL->{
            SimpleStruct struct = structCL.get(0);
            System.out.println(struct);
        });
    
        CKernel testStructKernel = configuration.createKernel("testStruct", simpleStruct);
        configuration.execute1DKernel(testStructKernel, 1, 1);
        
        simpleStruct.mapReadMemory(structCL->{
            SimpleStruct struct = structCL.get(0);
            System.out.println(struct);
        });
    }
}
