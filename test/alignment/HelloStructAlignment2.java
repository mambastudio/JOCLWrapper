package alignment;

import alignment.spatial.BoundingBox;
import alignment.spatial.Point4f;
import alignment.spatial.Point4i;
import coordinate.memory.type.LayoutGroup;
import coordinate.memory.type.LayoutMemory;
import coordinate.memory.type.LayoutValue;
import static coordinate.memory.type.LayoutValue.JAVA_INT;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
public class HelloStructAlignment2 {
    
    public static void main(String... args)
    {
        LayoutMemory point4i = LayoutGroup.groupValueLayout(
            LayoutValue.JAVA_INT.withId("x"),
            LayoutValue.JAVA_INT.withId("y"),
            LayoutValue.JAVA_INT.withId("z"),
            LayoutValue.JAVA_INT.withId("w"));
        
        LayoutMemory point4f = LayoutGroup.groupValueLayout(
            LayoutValue.JAVA_FLOAT.withId("x"),
            LayoutValue.JAVA_FLOAT.withId("y"),
            LayoutValue.JAVA_FLOAT.withId("z"),
            LayoutValue.JAVA_FLOAT.withId("w"));
        
        LayoutMemory hagrid = LayoutGroup.groupLayout(
            point4i.withId("grid_dims"),
            new BoundingBox().getLayout().withId("grid_bbox"),
            point4f.withId("cell_size"),
            JAVA_INT.withId("grid_shift")
        );        
        
        System.out.println(hagrid);        
    } 
    
    public static final long computeAlignmentOffset(long offset, long align)
    {
        return (offset + align - 1) & -align;
    }
}
