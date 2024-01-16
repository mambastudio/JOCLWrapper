/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

import alignment.spatial.Point2f;
import alignment.spatial.Point4f;
import alignment.spatial.Vector4f;
import coordinate.memory.type.LayoutGroup;
import coordinate.memory.type.LayoutMemory;
import coordinate.memory.type.LayoutMemory.PathElement;
import coordinate.memory.type.LayoutValue;
import coordinate.memory.type.MemoryRegion;
import coordinate.memory.type.StructBase;
import coordinate.memory.type.ValueState;

/**
 *
 * @author user
 */
public class Camera implements StructBase<Camera> {
    public Point4f position; 
    public Point4f lookat;
    public Vector4f up;
    public Point2f dimension;
    public float fov;
    
    private final static LayoutMemory layout = LayoutGroup.groupLayout(
            new Point4f().getLayout().withId("position"),
            new Point4f().getLayout().withId("lookat"),
            new Vector4f().getLayout().withId("up"),
            new Point2f().getLayout().withId("dimension"),
            LayoutValue.JAVA_FLOAT.withId("fov")            
    );  
    
    private static ValueState position_xState   = layout.valueState(PathElement.groupElement("position"), PathElement.groupElement("x"));
    private static ValueState position_yState   = layout.valueState(PathElement.groupElement("position"), PathElement.groupElement("y"));
    private static ValueState position_zState   = layout.valueState(PathElement.groupElement("position"), PathElement.groupElement("z"));
    private static ValueState position_wState   = layout.valueState(PathElement.groupElement("position"), PathElement.groupElement("w"));
    
    private static ValueState lookat_xState     = layout.valueState(PathElement.groupElement("lookat"), PathElement.groupElement("x"));
    private static ValueState lookat_yState     = layout.valueState(PathElement.groupElement("lookat"), PathElement.groupElement("y"));
    private static ValueState lookat_zState     = layout.valueState(PathElement.groupElement("lookat"), PathElement.groupElement("z"));
    private static ValueState lookat_wState     = layout.valueState(PathElement.groupElement("lookat"), PathElement.groupElement("w"));
    
    private static ValueState up_xState         = layout.valueState(PathElement.groupElement("up"), PathElement.groupElement("x"));
    private static ValueState up_yState         = layout.valueState(PathElement.groupElement("up"), PathElement.groupElement("y"));
    private static ValueState up_zState         = layout.valueState(PathElement.groupElement("up"), PathElement.groupElement("z"));
    private static ValueState up_wState         = layout.valueState(PathElement.groupElement("up"), PathElement.groupElement("w"));

    private static ValueState dimension_xState  = layout.valueState(PathElement.groupElement("dimension"), PathElement.groupElement("x"));
    private static ValueState dimension_yState  = layout.valueState(PathElement.groupElement("dimension"), PathElement.groupElement("y"));
    
    private static ValueState fov_State         = layout.valueState(PathElement.groupElement("fov"));
    
    public Camera()
    {
        position = new Point4f();
        lookat = new Point4f();
        dimension = new Point2f();
        up = new Vector4f();     
    }
    
    public Camera(Point4f position, Point4f lookat, Vector4f up, Point2f dimension, float fov)
    {
        this.position = position;
        this.lookat = lookat;
        this.up = up;
        this.dimension = dimension;
        this.fov = fov;        
    }

    public void setPosition(Point4f position)
    {
        this.position = position;
    }

    public void setLookat(Point4f lookat)
    {
        this.lookat = lookat;
    }

    public void setUp(Vector4f up)
    {
        this.up = up;        
    }

    public void setDimension(Point2f dimension)
    {
        this.dimension = dimension;
    }

    public void setFov(float fov)
    {
        this.fov = fov;        
    }
    
    public boolean isSynched(Camera cameraStruct)
    {
        float x  = position.x;
        float x1 = cameraStruct.position.get(0);
        float y  = position.y;
        float y1 = cameraStruct.position.get(1);
        float z  = position.z;
        float z1 = cameraStruct.position.get(2);
        
        return (x == x1) && (y == y1) &&  (z == z1);
    }

    @Override
    public void fieldToMemory(MemoryRegion memory) {
        position_xState.set(memory, position.x); 
        position_yState.set(memory, position.y); 
        position_zState.set(memory, position.z); 
        position_wState.set(memory, position.w); 

        lookat_xState.set(memory, lookat.x);   
        lookat_yState.set(memory, lookat.y);   
        lookat_zState.set(memory, lookat.z);   
        lookat_wState.set(memory, lookat.w);   

        up_xState.set(memory, up.x);       
        up_yState.set(memory, up.y);       
        up_zState.set(memory, up.z);       
        up_wState.set(memory, up.w);       

        dimension_xState.set(memory, dimension.x);
        dimension_yState.set(memory, dimension.y);

        fov_State.set(memory, fov);       
    }

    @Override
    public void memoryToField(MemoryRegion memory) {
        position.x = (float)(position_xState.get(memory));
        position.y = (float)(position_yState.get(memory));
        position.z = (float)(position_zState.get(memory));
        position.w = (float)(position_wState.get(memory));
        
        lookat.x = (float)(lookat_xState.get(memory));
        lookat.y = (float)(lookat_yState.get(memory));
        lookat.z = (float)(lookat_zState.get(memory));
        lookat.w = (float)(lookat_wState.get(memory));
        
        up.x = (float)(up_xState.get(memory));
        up.y = (float)(up_yState.get(memory));
        up.z = (float)(up_zState.get(memory));
        up.w = (float)(up_wState.get(memory));
        
        dimension.x = (float)(dimension_xState.get(memory));
        dimension.y = (float)(dimension_yState.get(memory));
        
        fov = (float)(fov_State.get(memory));
    }

    @Override
    public Camera newStruct() {
        return new Camera();
    }

    @Override
    public Camera copyStruct() {
        return new Camera(position, lookat, up, dimension, fov);
    }

    @Override
    public LayoutMemory getLayout() {
        return layout;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Camera: ").append("\n");
        builder.append("         eye    ").append(String.format("(%.5f, %.5f, %.5f)", position.get('x'), position.get('y'), position.get('z'))).append("\n");
        builder.append("         lookat ").append(String.format("(%.5f, %.5f, %.5f)", lookat.get('x'), lookat.get('y'), lookat.get('z'))).append("\n");
        builder.append("         up     ").append(String.format("(%.5f, %.5f, %.5f)", up.get('x'), up.get('y'), up.get('z'))).append("\n");
        builder.append("         fov    ").append(String.format("(%.5f)", fov));
        
        return builder.toString(); 
    }
}
