/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.scan;

import wrapper.core.algorithms.CPrefixSum;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class CLSource {
    public static String[] readFiles()
    {
        String source0      = CLFileReader.readFile(CPrefixSum.class, "Scan.cl"); 
        String source1      = CLFileReader.readFile(CLSource.class, "Util.cl");
        return new String[]{source0, source1} ;
    }
}
