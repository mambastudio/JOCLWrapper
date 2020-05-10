void printFloat(float v)
{
    printf("%4.12f\n", v);
}

//Paper - A Sound and Complete Abstraction for Reasoning about Parallel Prefix Sums
__kernel void koggeStone(__global const int* in, __global int* out, __local int* aux)
{
     int idl = get_local_id(0); // index in workgroup
     int idg = get_global_id(0);
     int lSize = get_local_size(0);

     aux[idl] = in[idg];
     barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);

     for(int offset = 1; offset < lSize; offset *= 2)
     {
          private int temp;
          if(idl >= offset) temp = aux[idl - offset];
          barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);
          if(idl >= offset) aux[idl] = temp + aux[idl];
          barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);

     }
     
     if(aux[idl] > 0)
       out[idg] = aux[idl - 1];
     else
       out[idg] = aux[0];
}
