/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package svm;

import coordinate.memory.type.LayoutValue;
import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import svm.algorithms.SVMMemoryManager;
import svm.algorithms.TestAlgorithms;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import wrapper.core.CKernel;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_ONLY;
import static wrapper.core.CNativeMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.svm.SVMNative;
import wrapper.util.CLFileReader;

/**
 *
 * @author jmburu
 */
public class SimpleJOCL {
    public static void main(String... args)
    {
        testSVMNew4();
    }
        
    public static void testSubMemory()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        SVMMemoryManager.configuration = configuration;
        
        cl_device_id device = configuration.getDevice().getId();
        long [] align = new long[1];
        CL.clGetDeviceInfo(device, CL.CL_DEVICE_MEM_BASE_ADDR_ALIGN, Sizeof.size_t, Pointer.to(align), null);
        System.out.println(Arrays.toString(align));
        CL.clGetDeviceInfo(device, CL.CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE, Sizeof.size_t, Pointer.to(align), null);
        System.out.println(Arrays.toString(align));
        
        long globalSize = 256;
        
        CNativeMemory<Int32> data = configuration.createBufferNative(new MemoryStruct(new Int32(), globalSize, false), READ_WRITE);
        SVMMemoryManager.fillOne(data);
        data.transferFromDevice();
        System.out.println(data.getMemory());
        
        CNativeMemory<Int32> subData = data.getSubBuffer(64, READ_ONLY);
        SVMMemoryManager.fillIndexReverse(subData);
        
        subData.transferFromDevice();
        System.out.println(subData.getMemory());
        
        data.transferFromDevice();
        System.out.println(data.getMemory());
        
        System.out.println(configuration.getDevice().hasSVMCapabilities());
    }
    
    public static void testSVMVariableInstantiation()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 10_000_000L;
        
        SVMNative<Int32> src = config.createSVM(new Int32(), globalSize);        
        src.forEach(i-> src.setStraight(i, new Int32()));
    }
    
    
    public static void testSVMOffset()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 10;
        
        SVMNative<Int32> src = config.createSVM(new Int32(), globalSize);
        
        SVMNative<Int32> srcSub = src.offsetIndex(5);
        srcSub.enqueueWrite();
        srcSub.setStraight(0, new Int32(43455));
        srcSub.enqueueUnmap();
        
        System.out.println(src);
        System.out.println(srcSub);
    }
    
    public static void testSVMNew()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 10;
        
        SVMNative<Int32> mem = config.createSVM(new Int32(), globalSize);
        for(int i = 0; i<globalSize; i++)
            mem.set(i, new Int32(i));
        System.out.println(mem);
        
        SVMNative<Int32> submem = mem.offsetIndex(5); 
        submem.setAll(new Int32(15));
        System.out.println(submem);
        
        System.out.println(mem);
        
        mem.copyFrom(submem);
        System.out.println(mem);
    }
    
    public static void testSVMNew2()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 5;
        SVMNative<Int32> memA = config.createSVM(new Int32(), globalSize);
        SVMNative<Int32> memB = config.createSVM(new Int32(), globalSize);
        SVMNative<Int32> memC = config.createSVM(new Int32(), globalSize);
        for(int i = 0; i<globalSize; i++)
        {
            memA.set(i, new Int32(i));
            memB.set(i, new Int32(i));
        }
        
        System.out.println(memA);
        System.out.println(memB);
        
        CKernel sampleKernel = config.createKernel("sampleKernel", memA, memB, memC);
        config.execute1DKernel(sampleKernel, globalSize, globalSize);
        
        System.out.println(memC);
    }
    
    public static void testSVMNew3()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        
        //global byteCapacity
        long globalSize = 5;
        SVMNative<Int32> memA = config.createSVM(new Int32(), globalSize);        
        for(int i = 0; i<globalSize; i++)        
            memA.getMemory().setAtIndex(LayoutValue.JAVA_INT, i, i); 
        
        System.out.println(memA);     
        
        CKernel sampleKernel = config.createKernel("sampleKernel2", memA);
        config.execute1DKernel(sampleKernel, globalSize, 1);
        Pointer pointer = memA.getPointer();
        IntBuffer buff = pointer.getByteBuffer(0, memA.getMemory().byteCapacity()).order(ByteOrder.nativeOrder()).asIntBuffer();
       
        System.out.println(buff.get(3));
        System.out.println(memA.getMemory().getAtIndex(LayoutValue.JAVA_INT, 2));
    }
    
    public static void testSVMNew4()
    {
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(CLFileReader.readFile(SimpleJOCL.class, "Hello.cl"));
        int n = 25;
        
        System.out.println(config.getDevice().getSVMCapabilities());
        
        SVMNative<Int32> memA = config.createSVM(new Int32(), n);   
                        
        CKernel sampleKernel = config.createKernel("sampleKernel2", memA);
        config.execute1DKernel(sampleKernel, n, n);
        
        System.out.println(memA);
        System.out.println(memA.offsetIndex(24));
                
        memA.offsetIndex(3).get();
    }
}
