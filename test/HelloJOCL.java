/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Random;
import wrapper.core.memory.values.FloatValue;
import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_ONLY;
import static wrapper.core.CMemory.WRITE_ONLY;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;
import wrapper.util.CLOptions;

/**
 *
 * @author user
 */
public class HelloJOCL {
    
    public static void main(String... args)
    {
        //setup configuration
        CLOptions options = CLOptions.include("test"); //#include
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(options, CLFileReader.readFile(HelloJOCL.class, "HelloCL.cl"));
       
        //global size
        int globalSize = 10;
        
        CMemory<FloatValue> aBuffer = configuration.createBufferF(FloatValue.class, globalSize, READ_ONLY);
        CMemory<FloatValue> bBuffer = configuration.createBufferF(FloatValue.class, globalSize, READ_ONLY);
        CMemory<FloatValue> cBuffer = configuration.createBufferF(FloatValue.class, globalSize, WRITE_ONLY);  
        
        aBuffer.mapWriteIterator(values -> {
            Random rnd = new Random(System.currentTimeMillis());
            for(FloatValue value : values)
                value.set(rnd.nextFloat()*100);      
        });        
        bBuffer.mapWriteIterator(array -> {
            Random rnd = new Random(System.currentTimeMillis());
            for(FloatValue values : array)
                values.set(rnd.nextFloat()*100);    
        });
        
        
        //final long workGroupSize[] = new long[1];        
        //Pointer workGroupSizePointer = Pointer.to(workGroupSize);
        //CL.clGetKernelWorkGroupInfo(vectorAdd.getId(), device.getId(), CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE, Sizeof.cl_long, workGroupSizePointer, workGroupSize);
        //System.out.println(workGroupSize[0]);
        
        //execute kernel
        CKernel vectorAdd = configuration.createKernel("VectorAdd", aBuffer, bBuffer, cBuffer);
        configuration.execute1DKernel(vectorAdd, globalSize, globalSize);
        
        cBuffer.mapReadIterator(values->{
            for(FloatValue value : values)
                System.out.print(value.v+ " ");
        });
                
        CResourceFactory.releaseAll();
      
    }    
}
