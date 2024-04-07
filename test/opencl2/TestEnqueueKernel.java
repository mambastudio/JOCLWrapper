/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencl2;

import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestEnqueueKernel {
    public static void main(String... args)
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestEnqueueKernel.class, "TestEnqueueKernel.cl"));
    }
}
