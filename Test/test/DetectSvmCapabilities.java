/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import org.jocl.CL;
import static org.jocl.CL.CL_DEVICE_NAME;
import static org.jocl.CL.CL_DEVICE_SVM_CAPABILITIES;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_PLATFORM_NAME;
import static org.jocl.CL.CL_PLATFORM_VERSION;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetDeviceInfo;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clGetPlatformInfo;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/**
 *
 * @author user
 */
public class DetectSvmCapabilities {
    public static void main(String[] args)
    {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
 
        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
       
        // Obtain all platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
       
        for (cl_platform_id platform : platforms)
        {
            String platformName = getString(platform, CL_PLATFORM_NAME);
            System.out.println("Platform: " + platformName);
           
            float clVersion = getOpenCLVersion(platform);
            System.out.println("  CL version: " + clVersion);            
            if (clVersion < 2.0)
            {
                System.out.println("  (no SVM support)");
                continue;
            }
           
            // Obtain the number of devices for the platform
            int numDevicesArray[] = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL,
                0, null, numDevicesArray);
            int numDevices = numDevicesArray[0];
           
            // Obtain the all device IDs
            cl_device_id allDevices[] = new cl_device_id[numDevices];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL,
                numDevices, allDevices, null);
   
            for (cl_device_id currentDevice : allDevices)
            {
                String deviceName = getString(currentDevice, CL_DEVICE_NAME);
                System.out.println("  Device: " + deviceName);
               
                long svmCapabilities[] = { 0 };
                clGetDeviceInfo(currentDevice, CL_DEVICE_SVM_CAPABILITIES,
                    Sizeof.cl_long, Pointer.to(svmCapabilities), null);
                String svmCapabilitiesString =
                    CL.stringFor_cl_device_svm_capabilities(
                        svmCapabilities[0]);
                System.out.println("    SVM capabilities: " +
                    svmCapabilitiesString);
            }
        }
    }
   
    private static float getOpenCLVersion(cl_platform_id platform)
    {
        String deviceVersion = getString(platform, CL_PLATFORM_VERSION);
        String versionString = deviceVersion.substring(7, 10);
        float version = Float.parseFloat(versionString);
        return version;
    }
   
    private static String getString(cl_device_id device, int paramName)
    {
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName,
            buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length-1);
    }
    private static String getString(cl_platform_id platform, int paramName)
    {
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName,
            buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length-1);
    }
}
