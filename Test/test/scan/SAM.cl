void printFloat(float v)
{
    printf("%4.12f\n", v);
}

void gpu_sync(int goalValue, global int* mutex)
{
     int lid = get_local_id(0);
     
     if(lid == 0)
     {
         atomic_inc(mutex);
         
         while(*mutex != goalValue-1)
         {
               //do nothing
         }
     }

}

static void global_sync(volatile __global int *flags)
{
    const size_t thread_id = get_local_id(0);
    const size_t workgroup_id = get_group_id(0);
 
    if (thread_id == 0) {
        atomic_or(&flags[workgroup_id], 1);
    }
 
    if (workgroup_id == 0) {
        if (thread_id < get_num_groups(0)) {
            while (atomic_or(&flags[thread_id], 0)) ;
        }
        barrier(CLK_GLOBAL_MEM_FENCE);
 
        if (thread_id < get_num_groups(0)) {
            flags[thread_id] = 0;
        }
    }
 
    if (thread_id == 0) {
        while (flags[workgroup_id] != 0) ;
    }
    barrier(CLK_GLOBAL_MEM_FENCE);
}

__kernel void test(global int* input, global int* output, global int* sums, volatile global int* flags)
{
     int gid = get_global_id(0);
     int bid = get_group_id(0);
     int lid = get_local_id(0);
     int bN  = get_num_groups(0);
     
     for(int i = 0; i<100000; i++)
     {
       
     }

     if(lid == 0)
     {
         int v1 = input[gid + 0];
         int v2 = input[gid + 1];
         int v3 = input[gid + 2];
         int v4 = input[gid + 3];
         
         output[gid + 0] = v1;
         output[gid + 1] = v1 + v2;
         output[gid + 2] = v1 + v2 + v3;
         output[gid + 3] = v1 + v2 + v3 + v4;
         
         sums[bid] = output[gid + 3];
         

     }


}

__kernel void sam(global int* input, global int* output, global int* sums, global int* flags)
{
     int gid = get_global_id(0);
     int bid = get_group_id(0);
     int lid = get_local_id(0);


     //simple SAM
     if(lid == 0)
     {
         int v1 = input[gid + 0];
         int v2 = input[gid + 1];
         int v3 = input[gid + 2];
         int v4 = input[gid + 3];
         
         output[gid + 0] = v1;
         output[gid + 1] = v1 + v2;
         output[gid + 2] = v1 + v2 + v3;
         output[gid + 3] = v1 + v2 + v3 + v4;
         
         sums[bid] = output[gid + 3];

         if(bid == 0)
         {
           flags[bid] = true;
         }

         if(bid > 0)
         {
            while(!flags[bid - 1])
            {

            }

            output[gid + 0] += sums[bid - 1];
            output[gid + 1] += sums[bid - 1];
            output[gid + 2] += sums[bid - 1];
            output[gid + 3] += sums[bid - 1];
            sums[bid]  =   output[gid + 3];
            flags[bid] = true;
         }
     }



}
