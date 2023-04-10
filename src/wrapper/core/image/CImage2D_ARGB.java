/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.image;

import javafx.scene.image.Image;
import static org.jocl.CL.CL_ARGB;
import static org.jocl.CL.CL_MEM_OBJECT_IMAGE2D;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_UNSIGNED_INT8;
import static org.jocl.CL.clCreateImage;
import org.jocl.cl_image_desc;
import org.jocl.cl_image_format;
import org.jocl.cl_mem;
import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public class CImage2D_ARGB extends CImage
{    
    int width, height;   
    
    cl_image_format imageFormat;
    cl_image_desc imageDesc;
    
    cl_mem imageMem;

    public CImage2D_ARGB(OpenCLConfiguration config, int width, int height) {
        super(config);        
       
        this.width = width;
        this.height = height;
        
        initImageVariables();
        
        imageMem = clCreateImage(
                config.getContext().getId(), 
                CL_MEM_READ_ONLY, 
                imageFormat, 
                imageDesc, 
                null,
                null);

    }
    
    public CImage2D_ARGB(OpenCLConfiguration config, Image image)
    {
        super(config);
                
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
        
        initImageVariables();
        
        imageMem = clCreateImage(
                config.getContext().getId(), 
                CL_MEM_READ_ONLY, 
                imageFormat, 
                imageDesc, 
                null,
                null);
    }
    
    private void initImageVariables()
    {
        int rowPitch = width * 4; // 4 bytes per pixel
        int slicePitch = 0;

        imageFormat = new cl_image_format();
        imageFormat.image_channel_order = CL_ARGB;
        imageFormat.image_channel_data_type = CL_UNSIGNED_INT8;
        
        imageDesc = new cl_image_desc();
        imageDesc.image_type = CL_MEM_OBJECT_IMAGE2D;
        imageDesc.image_width = width;
        imageDesc.image_height = height;
        imageDesc.image_depth = 0;
        imageDesc.image_array_size = 0;
        imageDesc.image_row_pitch = rowPitch;
        imageDesc.image_slice_pitch = slicePitch;
        imageDesc.num_mip_levels = 0;
        imageDesc.num_samples = 0;
        imageDesc.buffer = null;
        
        
    }
    
    public Image getImage()
    {
        return null;
    }

    public cl_mem getImageMemory()
    {
        return imageMem;
    }
}
