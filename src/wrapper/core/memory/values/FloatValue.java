/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory.values;

import coordinate.struct.FloatStruct;

/**
 *
 * @author user
 */
public class FloatValue extends FloatStruct{
    public float v = 0;
    
    public FloatValue()
    {
        v = 0;
    }
    
    public FloatValue(float value)
    {
        this.v = value;
    }
    
    public void set(float value) {
        this.v = value;
        this.refreshGlobalArray();
    }
}
