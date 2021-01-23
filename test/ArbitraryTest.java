
import org.jocl.struct.ArrayLength;
import org.jocl.struct.CLTypes.cl_float4;
import org.jocl.struct.Struct;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author user
 */
public class ArbitraryTest {
    public static void main(String... args)
    {
        Struct.showLayout(Struct1.class);
        
    }
    
    
    public static class Struct1 extends Struct
    {
        public float f;
        public char  c;
        
        @ArrayLength(2)
        public float[] array2; 
        
        public Struct2 struct;
        public Struct2 struct1;
    }
    
    public static class Struct2 extends Struct
    {
        public long d;
        public float f;
        public char  c;
    }
}
