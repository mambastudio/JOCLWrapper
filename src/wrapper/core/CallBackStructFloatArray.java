/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.FloatStruct;
import coordinate.struct.StructFloatArray;

/**
 *
 * @author user
 * @param <B>
 */
public interface CallBackStructFloatArray<B extends FloatStruct> {
    public void call(StructFloatArray<B> s);
}
