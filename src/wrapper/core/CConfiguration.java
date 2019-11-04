/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import java.util.ArrayList;
import java.util.Arrays;
import org.jocl.CL;
import static org.jocl.CL.CL_INVALID_VALUE;
import static org.jocl.CL.clGetPlatformIDs;
import org.jocl.cl_platform_id;

/**
 *
 * @author user
 */
public class CConfiguration 
{    
    private static final ArrayList<cl_platform_id> platforms = new ArrayList<>();
    
    static
    {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        initialize();
    }
    
    
    public static void initialize()
    {
        int numPlatforms = getPlatformNo();
        cl_platform_id platformArray[] = new cl_platform_id[numPlatforms];        
        int clGetPlatformIDs = clGetPlatformIDs(
                                       numPlatforms,                     //the number of entries that can be added to platforms
                                       platformArray,                    //list of Opencl found
                                       null);                            //the number of opencl platforms found
        if(clGetPlatformIDs == CL_INVALID_VALUE)
            throw new IllegalArgumentException("no device specified");
        else
            System.out.println("Platform available and a list has been made");
        
        platforms.clear();
        platforms.addAll(Arrays.asList(platformArray));
    }
    
    public static int getPlatformNo()
    {
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(
                0,                            //the number of entries that can be added to platforms
                null,                         //list of Opencl found
                numPlatformsArray);           //the number of opencl platforms found
        return numPlatformsArray[0];
    }
    
    public static cl_platform_id getPlatformId(int i)
    {
        return platforms.get(i);
    }
    
    public static CPlatform getPlatform(int i)
    {
        return new CPlatform(getPlatformId(i));
    } 
    
    public static CPlatform getDefault()
    {
        return new CPlatform(platforms.get(0));
    }
}
