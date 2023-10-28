/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nativememory.algorithms;

import coordinate.memory.type.MemoryStruct;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.utility.Timer;
import wrapper.core.CNativeMemory;
import wrapper.core.OpenCLConfiguration;
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
        
        CNativeMemory<MemoryStruct<Int32>> srcA = CLMemoryManager.fillOne(500_000_000);       
        System.out.println(srcA.getT().getMemoryReadableSize());
        
        CLPrefixSumInteger prefix = new CLPrefixSumInteger(configuration, srcA, true);
        Timer time = Timer.timeThis(()-> prefix.execute());
        System.out.println(time);
                
        srcA.transferFromDevice();
        System.out.println(String.format("%,d", srcA.getT().get(srcA.size()-1).value()));   
    }
    
    //butterfly sort integer
    public static void test2()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestAlgorithms.class, "AlgorithmsInt.cl"));
        CLMemoryManager.configuration = configuration;
        
        CNativeMemory<MemoryStruct<Int32>> cdata = CLMemoryManager.fillIndexReverse(16_000_000);
        //cdata.transferFromDevice();
        //System.out.println(cdata.getT());
        
        CLSortInteger sort = new CLSortInteger(configuration, cdata, true);
        Timer time = Timer.timeThis(()-> sort.execute());
        System.out.println(time);
        
        //cdata.transferFromDevice();
        //System.out.println(cdata.getT());
        
        cdata.transferFromDevice();
        System.out.println(String.format("%,d", cdata.getT().get(cdata.size()-1).value()));   
    }
}
