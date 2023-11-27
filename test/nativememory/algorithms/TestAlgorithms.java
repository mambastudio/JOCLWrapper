/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.LayoutValue;
import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.utility.Timer;
import wrapper.core.CMemorySkeleton;
import wrapper.core.CNativeMemory;
import static wrapper.core.CNativeMemory.READ_WRITE;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.SVMNative;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestAlgorithms {
    public static void main(String... args)
    {
        test1();
        
    }
    
    //prefixsum integer
    public static void test1()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        CNativeMemory<Int32> srcA = CLMemoryManager.fillOne(500_000_000);       
        System.out.println(srcA.getMemory().getMemoryReadableSize());
        
        CLPrefixSumInteger prefix = new CLPrefixSumInteger(configuration, srcA, true);
        Timer time = Timer.timeThis(()-> prefix.execute());
        System.out.println(time);
                
        srcA.transferFromDevice();
        System.out.println(String.format("%,d", srcA.getMemory().get(srcA.size()-1).value()));   
    }
    
    //butterfly sort integer
    public static void test2()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        CNativeMemory<Int32> cdata = CLMemoryManager.fillIndexReverse(16_000_000);
        //cdata.transferFromDevice();
        //System.out.println(cdata.getMemory());
        
        CLSortInteger sort = new CLSortInteger(configuration, cdata, true);
        Timer time = Timer.timeThis(()-> sort.execute());
        System.out.println(time);
        
        //cdata.transferFromDevice();
        //System.out.println(cdata.getMemory());
        
        cdata.transferFromDevice();
        System.out.println(String.format("%,d", cdata.getMemory().get(cdata.size()-1).value()));   
    }
    
    public static void test3()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        CNativeMemory<Int32> srcA = CLMemoryManager.fillOne(500_000_000);      
        CNativeMemory<Int32> result = configuration.createBufferNative(new MemoryStruct(new Int32(0)), READ_WRITE);
        
        System.out.println(srcA.getMemory().getMemoryReadableSize());
        
        CLReduceInteger reduce = new CLReduceInteger(configuration, srcA, result, true);
        Timer time = Timer.timeThis(()-> reduce.execute());
        System.out.println(time);
                
        reduce.cResult().transferFromDevice();
        System.out.println(reduce.cResult().getMemory());
        
        reduce.cResult().release();
    }
    
    //prefixsum integer
    public static void test4()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        SVMNative<Int32> srcA = configuration.createSVM(new Int32(), 500_000);
        
        CLMemoryManager.fillOne(srcA);    
        
        CPrefixSumInteger prefix = new CPrefixSumInteger(configuration, srcA);
        Timer time = Timer.timeThis(()-> prefix.execute());
        System.out.println(time);
                
        System.out.println(srcA.read(srcA.size()-1));   
    }
    
    //reduce (sum) integer
    public static void test5()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        
        CLMemoryManager.configuration = configuration;
        
        CMemorySkeleton<Int32> srcA = configuration.createSVM(new Int32(), 500_000_000);
        CMemorySkeleton<Int32> result = configuration.createSVMValue(new Int32(0));
        
        CLMemoryManager.fillOne(srcA);    
        
        CReduceInteger reduce = new CReduceInteger(configuration, srcA, result);
        Timer time = Timer.timeThis(()-> reduce.execute());
        System.out.println(time);
                
        System.out.println(result.read());   
    }
    
    //butterfly sort integer
    public static void test6()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        CMemorySkeleton<Int32> cdata = configuration.createSVM(new Int32(), 5_000_000);
        CLMemoryManager.fillIndexReverse(cdata);        
        
        CSortInteger sort = new CSortInteger(configuration, cdata);
        Timer time = Timer.timeThis(()-> sort.execute());
        System.out.println(time);
                
        System.out.println(cdata.read());   
    }
    
    //butterfly sort integer
    public static void test7()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        long n                          = 100_000_000;
        CMemorySkeleton<Int32> in       = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> out      = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> flags    = configuration.createSVM(new Int32(), n);
        
        CLMemoryManager.fillIndexInclusive(in);
        CLMemoryManager.fillOne(flags);
        //flags.write(memory -> memory.getMemory().setAtIndex(LayoutValue.JAVA_INT, 0, new int[]{1, 0, 0, 1, 0, 1, 1, 0}));
                
        CPartitionInteger partition = new CPartitionInteger(configuration, in, out, n, flags);
        Timer timer = Timer.timeThis(()->partition.execute());
        System.out.println(timer);
       // System.out.println(out);
        System.out.println(partition.partitionIndex());
    }
    
    //butterfly sort integer
    public static void test8()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        long n                              = 7;
        CMemorySkeleton<Int32> keys_in      = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> values_in    = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> keys_out     = configuration.createSVM(new Int32(), n);
        CMemorySkeleton<Int32> values_out   = configuration.createSVM(new Int32(), n);        
        
        keys_in.write(memory -> memory.getMemory().setAtIndex(LayoutValue.JAVA_INT, 0, new int[]{8, 6, 7, 5, 3, 0, 9}));
        values_in.write(memory -> memory.getMemory().setAtIndex(LayoutValue.JAVA_INT, 0, new int[]{0, 1, 2, 3, 4, 5, 6}));
                
        CSortPairsInteger sortPair = new CSortPairsInteger(configuration, keys_in, values_in, keys_out, values_out, n);
        Timer timer = Timer.timeThis(()->sortPair.execute());
        System.out.println(timer);
        
        System.out.println(keys_out);
        System.out.println(values_out);
    }
}
