/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package struct;

import coordinate.struct.annotation.arraysize;
import coordinate.struct.structbyte.StructBufferMemory;
import java.util.Arrays;

/**
 *
 * @author user
 */
public class SimpleStruct extends StructBufferMemory
{
    public int n;    
    @arraysize(5)
    public float[] arr; 
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("n   ").append(n).append("\n");
        builder.append("arr ").append(Arrays.toString(arr)).append("\n");
        
        return builder.toString();
    }
}
