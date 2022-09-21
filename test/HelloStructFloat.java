/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import wrapper.core.CKernel;
import wrapper.core.CMemory;
import static wrapper.core.CMemory.READ_WRITE;
import wrapper.core.CResourceFactory;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class HelloStructFloat {
    public static void main(String... args)
    {
        //setup configuration
        OpenCLConfiguration configuration = OpenCLConfiguration.getDefault(programSource);

        // size
        int n = 13;
        
        CMemory<Point3f> coordinatesBuffer = configuration.createBufferF(Point3f.class, n, READ_WRITE);

        //init kernel and execute
        CKernel kernel = configuration.createKernel("test", coordinatesBuffer);
        configuration.execute1DKernel(kernel, n, 1);
        coordinatesBuffer.transferFromDevice();
        
        for(int i = 0; i<n; i++)
        {
            System.out.println(coordinatesBuffer.get(i));
        }
        

        CResourceFactory.releaseAll();
    }
    
    private static final String programSource
            = // Definition of the Particle struct in the kernel            
            "__kernel void test(__global float4 *coordinates)" + "\n"
            + "{" + "\n"
            + "    int gid = get_global_id(0);" + "\n"
            + "    global float4* coord = coordinates + gid;" + "\n"
            + "    coord->x = gid;" + "\n"
            + "    coord->y = gid-1;" + "\n"
            + "    coord->z = 30;" + "\n"
            + "}";
}
