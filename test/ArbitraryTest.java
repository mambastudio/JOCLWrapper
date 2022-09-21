
import coordinate.struct.StructAbstract;
import coordinate.struct.annotation.arraysize;
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
        
        Struct2 s = new Struct2();
        System.out.println(s.getLayout());
    }
    
    
    public static class Struct1 extends Struct
    {
        public float f;
        public long  c;
           
    }
    
    public static class Struct2 extends StructAbstract
    {
        public float f;
        public long  c;
         
    }
}
