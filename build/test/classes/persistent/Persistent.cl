__kernel void persistent(__global int* ahead, __global int* bhead, __global int* count, __global int* a,  __global int* b)
{
    int lid = get_local_id(0);
    int gid = get_global_id(0);
    
    int lSize = get_local_size(0);
  



    do
    {
        if(lid == 0)
            *bhead = atomic_add(ahead, lSize);
            
        b[gid] = *ahead ;
        barrier(CLK_LOCAL_MEM_FENCE);
            
        if(*ahead >= *count)
            return;
      
    }while(true);
}
