/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.struct.structbyte.Structure;
import coordinate.struct.structbyte.StructureArray;


/**
 *
 * @author user
 * @param <B>
 */
public interface CallBackStructByteArray<B extends Structure> {
    public void call(StructureArray<B> s);
}
