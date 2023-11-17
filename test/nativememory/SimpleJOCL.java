/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory;

import wrapper.core.SVMNative;
import coordinate.memory.nativememory.NativeInteger;
import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.MemoryStructFactory.Long64;
import coordinate.memory.type.StructBase;
import java.util.Arrays;
import java.util.Random;
import nativememory.algorithms.CLMemoryManager;
import nativememory.algorithms.TestAlgorithms;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import wrapper.core.CKernel;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import static wrapper.core.CNativeMemory.READ_WRITE;
import static wrapper.core.CNativeMemory.WRITE_ONLY;
import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author jmburu
 */
public class SimpleJOCL {
    public static void main(String... args)
    {
        testSVM();
    }
    
    public static void testSimple()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 10L;
        
        MemoryStruct<Int32> srcA = new MemoryStruct(new Int32(), globalSize, false); 
        MemoryStruct<Int32> srcB = new MemoryStruct(new Int32(), globalSize, false);
        MemoryStruct<Int32> dest = new MemoryStruct(new Int32(), globalSize, false);
        
        
        CNativeMemory<Int32> nativeSrcA = configuration.createBufferNative(srcA, READ_ONLY);
        CNativeMemory<Int32> nativeSrcB = configuration.createBufferNative(srcB, READ_ONLY);
        CNativeMemory<Int32> nativeDest = configuration.createBufferNative(dest, WRITE_ONLY);
        
        Random rnd = new Random(System.currentTimeMillis());
        
       // nativeSrcA.transferToDevice();
       // nativeSrcB.transferToDevice();
                        
        nativeSrcA.write(n -> n.forEachSet((t, i)-> new Int32((int)i))); 
        nativeSrcB.write(n -> n.forEachSet((t, i) ->new Int32((int)i)));
       
        System.out.println(srcA);
        System.out.println(srcB);
        
        //execute kernel
        CKernel vectorAdd = configuration.createKernel("sampleKernel", nativeSrcA, nativeSrcB, nativeDest);
        configuration.execute1DKernel(vectorAdd, globalSize, 100);
        
        nativeDest.read(n -> System.out.println(n));
                
     //   nativeSrcA.write(n -> n.forEachSet((t, i) ->new Int32(rnd.nextInt(5))));   
      //  System.out.println(nativeSrcA.getMemory());
        
        configuration.execute1DKernel(vectorAdd, globalSize, 100);
        nativeDest.transferFromDevice();
        //   nativeDest.read(n -> System.out.println(n));
    }
    
    public static void testSubMemory()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        cl_device_id device = configuration.getDevice().getId();
        long [] align = new long[1];
        CL.clGetDeviceInfo(device, CL.CL_DEVICE_MEM_BASE_ADDR_ALIGN, Sizeof.size_t, Pointer.to(align), null);
        System.out.println(Arrays.toString(align));
        CL.clGetDeviceInfo(device, CL.CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE, Sizeof.size_t, Pointer.to(align), null);
        System.out.println(Arrays.toString(align));
        
        long globalSize = 256;
        
        CNativeMemory<Int32> data = configuration.createBufferNative(new MemoryStruct(new Int32(), globalSize, false), READ_WRITE);
        CLMemoryManager.fillOne(data);
        data.transferFromDevice();
        System.out.println(data.getMemory());
        
        CNativeMemory<Int32> subData = data.getSubBuffer(64, READ_ONLY);
        CLMemoryManager.fillIndexReverse(subData);
        
        subData.transferFromDevice();
        System.out.println(subData.getMemory());
        
        data.transferFromDevice();
        System.out.println(data.getMemory());
        
        System.out.println(configuration.getDevice().hasSVMCapabilities());
    }
    
    public static void testSVM()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 20L;
        long localSize = 10L;
        
        SVMNative<Int32> srcA = config.createSVM(new Int32(), globalSize);
        SVMNative<Int32> srcB = config.createSVM(new Int32(), globalSize);
        SVMNative<Int32> dest = config.createSVM(new Int32(), globalSize);
        
        srcA.write((t, i)-> new Int32((int)i)); 
        srcB.write((t, i) ->new Int32((int)i));
       
        System.out.println(srcA);
        System.out.println(srcB);
        
        //execute kernel
        CKernel vectorAdd = config.createKernel("sampleKernel", srcA, srcB, dest);
        config.execute1DKernel(vectorAdd, globalSize, localSize);
        
        System.out.println(dest);
        
        SVMNative<Int32> destSub = dest.offsetIndex(5);
        
        CKernel fillOne = config.createKernel("InitArrayIntOne", destSub);
        config.execute1DKernel(fillOne, 15, 5);
        
        System.out.println(dest);
        
    }
    
}
