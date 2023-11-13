/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.MemoryStructFactory.Float32;
import coordinate.memory.type.MemoryStructFactory.Int32;
import coordinate.memory.type.StructBase;

/**
 *
 * @author user
 * @param <S>
 */
public class CNativeMemoryLocal<S extends StructBase> extends CNativeMemory<S> {
    
    public static CNativeMemoryLocal<Int32>     LOCALINT        =   new CNativeMemoryLocal(new Int32());
    public static CNativeMemoryLocal<Float32>   LOCALFLOAT      =   new CNativeMemoryLocal(new Float32());
    
    public CNativeMemoryLocal(S data) {
        super(null, null, null, null, data.sizeOf());
    }
}
