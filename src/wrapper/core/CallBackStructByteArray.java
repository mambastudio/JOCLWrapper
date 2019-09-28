/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.ByteStruct;
import coordinate.struct.StructByteArray;

/**
 *
 * @author user
 * @param <B>
 */
public interface CallBackStructByteArray<B extends ByteStruct> {
    public void call(StructByteArray<B> s);
}
