/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.image;

import coordinate.struct.structbyte.StructBufferMemory;

/**
 *
 * @author user
 */
public class C_ARGB extends StructBufferMemory{
    public int a, r, g, b;
    
    public C_ARGB()
    {
        a = r = g = b;
    }
    
    public void set(int a, int r, int g, int b)
    {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
        this.refreshGlobalBuffer();
    }
}
