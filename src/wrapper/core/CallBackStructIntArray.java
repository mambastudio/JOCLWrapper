/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.structint.IntStruct;
import coordinate.struct.structint.StructIntArray;

/**
 *
 * @author user
 * @param <B>
 */
public interface CallBackStructIntArray <B extends IntStruct> {
    public void call(StructIntArray<B> s);
}
