

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


import coordinate.generic.SCoord;
import coordinate.struct.FloatStruct;

/**
 *
 * @author user
 */
public class Point3f extends FloatStruct implements SCoord<Point3f, Vector3f> 
{    
    public float x, y, z;
    public Point3f() {
        super();
    }

    public Point3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3f(Point3f p) {
        x = p.x;
        y = p.y;
        z = p.z;
    }
            
    public static final Vector3f sub(Point3f p1, Point3f p2) 
    {
        Vector3f dest = new Vector3f();
        dest.x = p1.x - p2.x;
        dest.y = p1.y - p2.y;
        dest.z = p1.z - p2.z;
        return dest;
    }

    public static final Point3f mid(Point3f p1, Point3f p2) 
    {
        Point3f dest = new Point3f();
        dest.x = 0.5f * (p1.x + p2.x);
        dest.y = 0.5f * (p1.y + p2.y);
        dest.z = 0.5f * (p1.z + p2.z);
        return dest;
    }
    

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public float[] getArray() {
        return new float[]{x, y, z, 0};
    }

    @Override
    public void set(float... values) {
        this.setValue(values[0], values[1], values[2]);
        this.refreshGlobalArray();
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
    public Point3f copy() {
        return new Point3f(x, y, z);
    }

    @Override
    public Point3f getSCoordInstance() {
        return new Point3f();
    }

    @Override
    public Vector3f getVCoordInstance() {
        return new Vector3f();
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
    
    @Override
    public String toString()
    {
        float[] array = getArray();
        return String.format("(%3.2f, %3.2f, %3.2f)", array[0], array[1], array[2]);
    }
}
