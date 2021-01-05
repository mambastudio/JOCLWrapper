void printFloat(float v)
{
    printf("%4.8f\n", v);
}

int get(int index, int length, global int* data)
{
    if(index >= length)
        return INT_MAX;
    else 
        return data[index];
}

bool isGreaterThan(int posStart, int posEnd, int length, global int* data)
{
    int value1 = get(posStart, length, data);
    int value2 = get(posEnd, length, data);

    return value1 > value2;
}

void swap(int PosSIndex, int PosEIndex, global int* data)
{       
    int tmp          = data[PosSIndex];
    data[PosSIndex]  = data[PosEIndex];
    data[PosEIndex]  = tmp;
}


__kernel void butterfly1(global int* data, global int* lengthSize, global float* powerX)
{
     int gid = get_global_id(0);

     int t = gid;
     int radix = 2;
     int length = lengthSize[0];
     int PowerX = powerX[0];

     int yIndex      = (int) (t/(PowerX/radix));
     int kIndex      = (int) (t%(PowerX/radix));
     int PosStart    = (int) (kIndex + yIndex * PowerX);
     int PosEnd      = (int) (PowerX - kIndex - 1 + yIndex * PowerX);

     if(isGreaterThan(PosStart, PosEnd, length, data))
         swap(PosStart, PosEnd, data);
}

__kernel void butterfly2(global int* data, global int* lengthSize, global float* powerX)
{
    int gid = get_global_id(0);

    int t = gid;
    int radix = 2;
    int length = lengthSize[0];
    int PowerX = powerX[0];

    int yIndex      = (int) (t/(PowerX/radix));
    int kIndex      = (int) (t%(PowerX/radix));
    int PosStart    = (int) (kIndex + yIndex * PowerX);
    int PosEnd      = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                            
    if(isGreaterThan(PosStart, PosEnd, length, data))
        swap(PosStart, PosEnd, data);


}

int getInteger(int index, int length, global int* data)
{
    if(index >= length)
        return INT_MAX;
    else 
        return data[index];
}

bool isGreaterThanInteger(int posStart, int posEnd, int length, global int* data)
{
    int value1 = getInteger(posStart, length, data);
    int value2 = getInteger(posEnd, length, data);

    return value1 > value2;
}

void swapInteger(int PosSIndex, int PosEIndex, global int* data)
{       
    int tmp          = data[PosSIndex];
    data[PosSIndex]  = data[PosEIndex];
    data[PosEIndex]  = tmp;
}

__kernel void butterfly1Integer(global int* data, global int* lengthSize, global float* powerX)
{
     int gid = get_global_id(0);

     int t = gid;
     int radix = 2;
     int length = lengthSize[0];
     int PowerX = powerX[0];

     int yIndex      = (int) (t/(PowerX/radix));
     int kIndex      = (int) (t%(PowerX/radix));
     int PosStart    = (int) (kIndex + yIndex * PowerX);
     int PosEnd      = (int) (PowerX - kIndex - 1 + yIndex * PowerX);

     if(isGreaterThanInteger(PosStart, PosEnd, length, data))
         swapInteger(PosStart, PosEnd, data);
}

__kernel void butterfly2Integer(global int* data, global int* lengthSize, global float* powerX)
{
    int gid = get_global_id(0);

    int t = gid;
    int radix = 2;
    int length = lengthSize[0];
    int PowerX = powerX[0];

    int yIndex      = (int) (t/(PowerX/radix));
    int kIndex      = (int) (t%(PowerX/radix));
    int PosStart    = (int) (kIndex + yIndex * PowerX);
    int PosEnd      = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                            
    if(isGreaterThanInteger(PosStart, PosEnd, length, data))
        swapInteger(PosStart, PosEnd, data);


}

