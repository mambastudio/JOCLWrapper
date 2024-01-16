/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.core;

import coordinate.memory.type.StructBase;
import coordinate.memory.type.StructCache;
import java.util.Objects;
import org.jocl.Pointer;

/**
 *
 * @author jmburu
 * @param <T>
 * @param <S>
 */
public interface CMemorySkeleton<T extends StructBase, S extends StructCache<T, S>> extends StructCache<T, S>{
    public boolean isSVM();
    public Pointer getPointer();
    public boolean isSubMemory();  
    public void reallocate(long size);
    public boolean isFree();
    default boolean isLocal()
    {
        return Objects.isNull(getPointer());
    }        
}
