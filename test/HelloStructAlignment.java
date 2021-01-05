/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import org.jocl.struct.CLTypes.cl_float2;
import org.jocl.struct.CLTypes.cl_float4;
import org.jocl.struct.Struct;

/**
 *
 * @author user
 */
public class HelloStructAlignment {
    public static void main(String... args)
    {
        Struct.showLayout(Intersection.class);
    }
    
     public static class Intersection extends Struct
    {   
        public int i;
        public int j;
        public cl_float4 p;       //16 bytes
        public InnerStruct inner1;
        public cl_float2 uv;      //8 bytes      
        public cl_float4 p1;       //16 bytes
        public InnerStruct inner2;
        
        
    }
     
    public static class InnerStruct extends Struct
    {
        public cl_float4 p1;       //16 bytes
        public int i;
    }
}
