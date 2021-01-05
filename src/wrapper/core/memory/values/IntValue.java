/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core.memory.values;

import coordinate.struct.IntStruct;

/**
 *
 * @author user
 */
public class IntValue extends IntStruct{
    public int v = 0;
    
    public IntValue()
    {
        v = 0;
    }
    
    public IntValue(int value)
    {
        this.v = value;
    }
    
    
    public void set(int value) {
        this.v = value;
        this.refreshGlobalArray();
    }
    
    @Override
    public String toString()
    {
        return Integer.toString(v);
    }
}
