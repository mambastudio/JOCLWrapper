void printFloat(float v)
{
    printf("%4.2f\n", v);
}

__kernel void InitArrayIntOne(
    global int* array)
{
    uint global_id = get_global_id(0);
    array[global_id] = 1; 
}

__kernel void sampleKernel( __global const int *a,
                            __global const int *b,
                            __global int *c)
{
    int gid = get_global_id(0);
    c[gid] = a[gid] * b[gid];
}

__kernel void sampleKernel2( __global int *a)
{
    int gid = get_global_id(0);
    //printFloat(a[gid]);    
    a[gid] = 2191;
    
}

