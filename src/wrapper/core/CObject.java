/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import org.jocl.NativePointerObject;

/**
 *
 * @author user
 */
public abstract class CObject
{
    protected NativePointerObject id = null;
    
    protected CObject(NativePointerObject id)
    {
        this.id = id;
    }
    
    public NativePointerObject getId()
    {
        return id;
    }
}
