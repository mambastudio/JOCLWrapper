#include <Basic.h>


__kernel void VectorAdd(global float* a, global float* b, global float* c)
{
     int global_id = get_global_id(0);
     c[global_id] = (EMITTER == 1);
}
