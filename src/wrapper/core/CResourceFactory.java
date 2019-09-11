/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.util.HashMap;

/**
 *
 * @author user
 * 
 * OpenCL recognizes the following resources for release:
 *  - Memory objects (not for SVM, but there is an exception ~ check intel svm tutorial)
 *  - Context
 *  - Command Queue
 *  - Program
 *  - Kernels
 *  - Events (not implemented here yet)
 */

public class CResourceFactory {
    private static final HashMap<String, CProgram>  programResources = new HashMap<>();
    private static final HashMap<String, CKernel> kernelResources = new HashMap<>();
    private static final HashMap<String, CCommandQueue> queueResources = new HashMap<>();
    private static final HashMap<String, CContext> contextResources = new HashMap<>();
    private static final HashMap<String, CMemory> memoryResources = new HashMap<>();
    
    public static void registerProgram(String name, CProgram program)
    {
        programResources.put(name, program);
    }
    
    public static void registerKernel(String name, CKernel kernel)
    {
        kernelResources.put(name, kernel);
    }
    
    public static void registerCommandQueue(String name, CCommandQueue queue)
    {
        queueResources.put(name, queue);
    }
    
    public static void registerContext(String name, CContext context)
    {
        contextResources.put(name, context);
    }
    
    public static void registerMemory(String name, CMemory memory)
    {
        memoryResources.put(name, memory);
    }
    
    public static void releaseAll()
    {
        contextResources.values().forEach((context) -> context.release());
        contextResources.clear();
        
        kernelResources.values().forEach((kernel) -> kernel.release());
        kernelResources.clear();
        
        memoryResources.values().forEach((memory) -> memory.release());
        memoryResources.clear();
        
        programResources.values().forEach((program) -> program.release());
        programResources.clear();
        
        queueResources.values().forEach((queue) -> queue.release());
        queueResources.clear();
    }
    
    public static void releaseMemory(String... names)
    {
        for(String memoryName : names)
            if(memoryResources.containsKey(memoryName))
            {
                memoryResources.get(memoryName).release();
                memoryResources.remove(memoryName);
            }
    }
}
