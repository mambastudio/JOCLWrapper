/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

import alignment.spatial.BoundingBox;
import alignment.spatial.Point2f;
import alignment.spatial.Point4f;
import alignment.spatial.Point4i;
import alignment.spatial.Vector4f;
import static coordinate.utility.BitUtility.next_multipleof;
import org.jocl.struct.CLTypes.cl_float2;
import org.jocl.struct.CLTypes.cl_float4;
import org.jocl.struct.Struct;
import wrapper.core.CKernel;
import wrapper.core.OpenCLConfiguration;
import wrapper.core.svm.SVMNative;
import wrapper.util.CLFileReader;

/**
 *
 * @author user
 */
public class TestAlignmentCL {
    
    public static void main(String... args)
    {
        testCamera();
    }
    
    public static void testHagrid()
    {
        System.out.println(new Hagrid().getLayout());
        
        long size = 1;
        long local_size = 1;
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(readFiles());
        
        SVMNative<Hagrid> hagrids = config.createSVM(new Hagrid(), 1);
        Hagrid hagrid = new Hagrid();  
        hagrid.grid_dims = new Point4i(3, 4, 334);
        hagrid.cell_size = new Point4f(3, 23, 3);
        hagrid.grid_bbox = new BoundingBox(new Point4f(3, 3, 5), new Point4f(1, 3, 4));
        hagrid.grid_shift = 4;
        
        hagrids.set(0, hagrid);
        System.out.println(hagrids.get().grid_bbox);
        
        CKernel structAlignmentTest = config.createKernel("Test", hagrids);
        config.execute1DKernel(structAlignmentTest, next_multipleof(size, local_size), local_size);
    }
    
    public static void testCamera()
    {
        System.out.println(new Camera().getLayout());
        Struct.showLayout(Cam.class);
        
        long size = 1;
        long local_size = 1;
        OpenCLConfiguration config = OpenCLConfiguration.getDefault(readFiles());
        
        SVMNative<Camera> cameraCL = config.createSVM(new Camera(), 1);
        Camera camera = new Camera();
        camera.position = new Point4f(0, 0, -9);
        camera.lookat = new Point4f();
        camera.up = new Vector4f(0, 1, 0);
        camera.dimension = new Point2f(500, 500);
        camera.fov = 45;
        
        cameraCL.set(camera);
        System.out.println(cameraCL.get());
        
        CKernel structAlignmentTest = config.createKernel("TestCamera", cameraCL);
        config.execute1DKernel(structAlignmentTest, next_multipleof(size, local_size), local_size);
    }
    
    public static String[] readFiles()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CLFileReader.readFile(TestAlignmentCL.class, "TestStruct.cl"));
        return new String[]{stringBuilder.toString()};
    }
    
    private static class Cam extends Struct
    {
        public cl_float4 position; 
        public cl_float4 lookat;
        public cl_float4 up;
        public cl_float2 dimension;
        public float fov;
    }
}
