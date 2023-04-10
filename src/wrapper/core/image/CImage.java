/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.image;

import wrapper.core.OpenCLConfiguration;

/**
 *
 * @author user
 */
public abstract class CImage {
    protected final OpenCLConfiguration config;
    
    public CImage(OpenCLConfiguration config)
    {
        this.config = config;
    }
}
