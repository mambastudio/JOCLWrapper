

/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


import coordinate.generic.VCoord;

/**
 *
 * @author user
 */
public class Vector3f implements VCoord<Vector3f>
{    
    public float x, y, z;
    
    public Vector3f() {x = 0; y = 0; z = 0;}
    public Vector3f(float a) {x = a; y = a; z = a;}
    public Vector3f(float a, float b, float c) {x = a; y = b; z = c;}
    public Vector3f(Vector3f a) {x = a.x; y = a.y; z = a.z;}
    
    public static Vector3f cross(Vector3f a, Vector3f b)
    {
        return a.cross(b);
    }
          
    public void addAssign(int i, float value) {float oldValue = get(i); set(i, oldValue + value);}
    public void mulAssign(int i, float value) {float oldValue = get(i); set(i, oldValue * value);}
    
    @Override
    public Vector3f normalize()
    {
        return normalize(this);
    }
    
    public static Vector3f add(Vector3f a, Vector3f b) {return a.add(b);}    
    public static Vector3f mul(Vector3f a, Vector3f b) {return a.mul(b);}
    public static Vector3f div(Vector3f a, Vector3f b) {return a.div(b);}
    public static float dot(Vector3f a, Vector3f b) {return a.x*b.x + a.y*b.y + a.z*b.z;}
    public static float absDot(Vector3f a, Vector3f b) {return Math.abs(a.x*b.x + a.y*b.y + a.z*b.z);}
    
    public static Vector3f normalize(Vector3f a)
    {
        Vector3f dest = a.clone();
        float lenSqr = Vector3f.dot(a, a);
        float len    = (float) java.lang.Math.sqrt(lenSqr);
        
        dest.x = a.x/len;
        dest.y = a.y/len;
        dest.z = a.z/len;
        return dest;        
    }
        
    @Override
    public float get(int i)
    {
        switch (i) {
            case 0:
                if((1f/x == Float.NEGATIVE_INFINITY)) return 0; else return x;
            case 1:
                if((1f/y == Float.NEGATIVE_INFINITY)) return 0; else return y;
            case 2:
                if((1f/z == Float.NEGATIVE_INFINITY)) return 0; else return z;
            default:
                throw new UnsupportedOperationException("Invalid");
        }
    }
    
    public void set(int i, float value)
    {
        switch (i) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            case 2:
                z = value;
                break;
            default:
                throw new UnsupportedOperationException("Invalid");
        }
    }
    
    public void set(float value)
    {
        x = y = z = value;
    }
    
    @Override
    public void setValue(Vector3f v)
    {
        x = v.x; y = v.y; z = v.z;
    }
        
    public boolean isZero()
    {
        return x == 0 && y == 0 && z == 0;
    }
    
    public float max()
    {
        float a = x, b = y, c = z;
        if (a < b)
            a = b;
        if (a < c)
            a = c;
        return a;    
    }
    
    @Override
    public Vector3f clone() 
    {
        return new Vector3f(x, y, z);
    }
    
    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public float[] getArray() {
        return new float[]{x, y, z};
    }

    @Override
    public void set(float... values) {
        x = values[0];
        y = values[1];
        z = values[2];
    }

    @Override
    public float get(char axis) {
        switch (axis) {
            case 'x':
                return x;
            case 'y':
                return y;
            case 'z':
                return z;
            default:
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.            
        }
    }

    @Override
    public void set(char axis, float value) {
        switch (axis) {
            case 'x':
                x = value;
                break;
            case 'y':
                y = value;
                break;
            case 'z':
                z = value;
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public Vector3f getCoordInstance() {
        return new Vector3f();
    }

    @Override
    public Vector3f copy() {
        return new Vector3f(x, y, z);
    }

    @Override
    public void setIndex(int index, float value) {
        switch (index)
        {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;    
            case 2:
                z = value;
                break;
        }
    }

    @Override
    public int getByteSize() {
        return 4;
    }

}
