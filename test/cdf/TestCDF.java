/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdf;

import wrapper.core.OpenCLConfiguration;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestCDF {
    public static void main(String... args)
    {
        test1();
    }
    
    public static void test1()
    {
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(CLFileReader.readFile(TestCDF.class, "CDF2DImplementation.cl"));
    }
}
