/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

import alignment.spatial.BoundingBox;
import alignment.spatial.Point4f;
import alignment.spatial.Point4i;
import coordinate.memory.type.LayoutGroup;
import coordinate.memory.type.LayoutMemory;
import static coordinate.memory.type.LayoutValue.JAVA_INT;
import coordinate.memory.type.MemoryRegion;
import coordinate.memory.type.StructBase;
import coordinate.memory.type.ValueState;

/**
 *
 * @author user
 */
public class Hagrid implements StructBase<Hagrid> {       
    public Point4i         grid_dims;
    public BoundingBox     grid_bbox;
    public Point4f         cell_size;
    public int             grid_shift;
        
    private static final LayoutMemory layout = LayoutGroup.groupLayout(
            new Point4i().getLayout().withId("grid_dims"),
            new BoundingBox().getLayout().withId("grid_bbox"),
            new Point4f().getLayout().withId("cell_size"),
            JAVA_INT.withId("grid_shift")
    );   
    
    private static ValueState grid_dims_xState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_dims"), LayoutMemory.PathElement.groupElement("x"));
    private static ValueState grid_dims_yState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_dims"), LayoutMemory.PathElement.groupElement("y"));
    private static ValueState grid_dims_zState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_dims"), LayoutMemory.PathElement.groupElement("z"));
    private static ValueState grid_dims_wState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_dims"), LayoutMemory.PathElement.groupElement("w"));
    
    private static ValueState bbox_min_xState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("minimum"), LayoutMemory.PathElement.groupElement("x"));
    private static ValueState bbox_min_yState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("minimum"), LayoutMemory.PathElement.groupElement("y"));
    private static ValueState bbox_min_zState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("minimum"), LayoutMemory.PathElement.groupElement("z"));
    private static ValueState bbox_min_wState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("minimum"), LayoutMemory.PathElement.groupElement("w"));
    
    private static ValueState bbox_max_xState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("maximum"), LayoutMemory.PathElement.groupElement("x"));
    private static ValueState bbox_max_yState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("maximum"), LayoutMemory.PathElement.groupElement("y"));
    private static ValueState bbox_max_zState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("maximum"), LayoutMemory.PathElement.groupElement("z"));
    private static ValueState bbox_max_wState = layout.valueState(LayoutMemory.PathElement.groupElement("grid_bbox"), LayoutMemory.PathElement.groupElement("maximum"), LayoutMemory.PathElement.groupElement("w"));
    
    private static ValueState cell_size_xState = layout.valueState(LayoutMemory.PathElement.groupElement("cell_size"), LayoutMemory.PathElement.groupElement("x"));
    private static ValueState cell_size_yState = layout.valueState(LayoutMemory.PathElement.groupElement("cell_size"), LayoutMemory.PathElement.groupElement("y"));
    private static ValueState cell_size_zState = layout.valueState(LayoutMemory.PathElement.groupElement("cell_size"), LayoutMemory.PathElement.groupElement("z"));
    private static ValueState cell_size_wState = layout.valueState(LayoutMemory.PathElement.groupElement("cell_size"), LayoutMemory.PathElement.groupElement("w"));
    
    private static ValueState grid_shift_state = layout.valueState(LayoutMemory.PathElement.groupElement("grid_shift"));
    
    public Hagrid()
    {
        this.grid_dims = new Point4i();
        this.grid_bbox = new BoundingBox();
        this.cell_size = new Point4f();
        this.grid_shift = 0;
    }
    
    public Hagrid(Point4i grid_dims, BoundingBox grid_bbox, Point4f cell_size, int grid_shift)
    {
        this.grid_dims  = grid_dims; 
        this.grid_bbox  = grid_bbox; 
        this.cell_size  = cell_size; 
        this.grid_shift = grid_shift;
    }

    @Override
    public void fieldToMemory(MemoryRegion memory) {
        grid_dims_xState.set(memory, grid_dims.x);
        grid_dims_yState.set(memory, grid_dims.y);
        grid_dims_zState.set(memory, grid_dims.z);
        grid_dims_wState.set(memory, grid_dims.w);

        bbox_min_xState.set(memory, grid_bbox.minimum.x);
        bbox_min_yState.set(memory, grid_bbox.minimum.y);
        bbox_min_zState.set(memory, grid_bbox.minimum.z);
        bbox_min_wState.set(memory, grid_bbox.minimum.w);

        bbox_max_xState.set(memory, grid_bbox.maximum.x); 
        bbox_max_yState.set(memory, grid_bbox.maximum.y); 
        bbox_max_zState.set(memory, grid_bbox.maximum.z); 
        bbox_max_wState.set(memory, grid_bbox.maximum.w); 

        cell_size_xState.set(memory, cell_size.x);
        cell_size_yState.set(memory, cell_size.y);
        cell_size_zState.set(memory, cell_size.z);
        cell_size_wState.set(memory, cell_size.w);

        grid_shift_state.set(memory, grid_shift);

    }

    @Override
    public void memoryToField(MemoryRegion memory) {
        grid_dims.x = (int)grid_dims_xState.get(memory);
        grid_dims.y = (int)grid_dims_yState.get(memory);
        grid_dims.z = (int)grid_dims_zState.get(memory);
        grid_dims.w = (int)grid_dims_wState.get(memory);
        
        grid_bbox.minimum.x = (float)bbox_min_xState.get(memory);
        grid_bbox.minimum.y = (float)bbox_min_yState.get(memory);
        grid_bbox.minimum.z = (float)bbox_min_zState.get(memory);
        grid_bbox.minimum.w = (float)bbox_min_wState.get(memory);
                
        grid_bbox.maximum.x = (float)bbox_max_xState.get(memory);
        grid_bbox.maximum.y = (float)bbox_max_yState.get(memory);
        grid_bbox.maximum.z = (float)bbox_max_zState.get(memory);
        grid_bbox.maximum.w = (float)bbox_max_wState.get(memory);   
        
        cell_size.x = (float)cell_size_xState.get(memory);
        cell_size.y = (float)cell_size_yState.get(memory);
        cell_size.z = (float)cell_size_zState.get(memory);
        cell_size.w = (float)cell_size_wState.get(memory);
        
        grid_shift = (int)grid_shift_state.get(memory);
    }

    @Override
    public Hagrid newStruct() {
        return new Hagrid();
    }

    @Override
    public Hagrid copyStruct() {
        return new Hagrid(
                grid_dims.copyStruct(), 
                grid_bbox.copyStruct(), 
                cell_size.copyStruct(), 
                grid_shift);
    }

    @Override
    public LayoutMemory getLayout() {
        return layout;
    }
    
}
