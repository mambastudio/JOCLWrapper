#define SWAP(a,b) {__local int *tmp=a;a=b;b=tmp;}
#define getInput(index) (getData(index, length, data))
#define setOutput(index, value) (setData(index, value, length, data))

/*
  https://github.com/LariscusObscurus/HPC_Exercise/blob/master/src/kernels/scan.cl
*/

int getData(int index, global int* length, global int* data)
{
    if(index >= *length)
        return 0;
    else 
        return data[index];
}

void setData(int index, int value, global int* length, global int* data)
{
    if(index >= *length)
        return;
    else 
        data[index] = value;
}

__kernel void  blelloch_scan_g(global int* data,
                               global int* group_sum,
                               global int* length,
                               local  int* temp)
{
     uint global_id = get_global_id ( 0 );
     uint local_id = get_local_id ( 0 );

     uint group_id = get_group_id ( 0 );
     uint group_size = get_local_size ( 0 );

     uint depth = 1 ;

     temp [local_id] = getInput(global_id);
     
     // upsweep   
     for ( uint stride = group_size >> 1 ; stride> 0 ; stride >>= 1 )
     {
          barrier (CLK_LOCAL_MEM_FENCE);
  
          if (local_id <stride) 
          {
              uint i = depth * ( 2 * local_id + 1 ) - 1 ;
              uint j = depth * ( 2 * local_id + 2 ) - 1 ;
              temp [j] += temp [i];
          }
  
          depth <<= 1 ;
     }

     // set identity before downsweep
     if (local_id == 0 )
     {
        group_sum[group_id] = temp[group_size - 1];
        temp [group_size - 1] = 0 ;
     }

     // downsweep
     for (uint stride = 1 ; stride <group_size; stride <<= 1 ) {

        depth >>= 1 ;
        barrier (CLK_LOCAL_MEM_FENCE);

        if (local_id <stride) {
            uint i = depth * (2 * local_id + 1 ) - 1 ;
            uint j = depth * (2 * local_id + 2 ) - 1 ;

            int t = temp [j];
            temp [j] += temp [i];
            temp [i] = t;
        }
    }

    barrier (CLK_LOCAL_MEM_FENCE);
    setOutput(global_id, temp[local_id]);
}

__kernel void  blelloch_scan(global int* data,
                             global int* length,
                             local  int* temp)
{
     uint global_id = get_global_id ( 0 );
     uint local_id = get_local_id ( 0 );

     uint group_id = get_group_id ( 0 );
     uint group_size = get_local_size ( 0 );

     uint depth = 1 ;

     temp [local_id] = getInput(global_id);
     
     // upsweep   
     for ( uint stride = group_size >> 1 ; stride> 0 ; stride >>= 1 )
     {
          barrier (CLK_LOCAL_MEM_FENCE);
  
          if (local_id <stride) 
          {
              uint i = depth * ( 2 * local_id + 1 ) - 1 ;
              uint j = depth * ( 2 * local_id + 2 ) - 1 ;
              temp [j] += temp [i];
          }
  
          depth <<= 1 ;
     }

     // set identity before downsweep
     if (local_id == 0 )
        temp [group_size - 1] = 0 ;

     // downsweep
     for (uint stride = 1 ; stride <group_size; stride <<= 1 ) {

        depth >>= 1 ;
        barrier (CLK_LOCAL_MEM_FENCE);

        if (local_id <stride) {
            uint i = depth * (2 * local_id + 1 ) - 1 ;
            uint j = depth * (2 * local_id + 2 ) - 1 ;

            int t = temp [j];
            temp [j] += temp [i];
            temp [i] = t;
        }
    }

    barrier (CLK_LOCAL_MEM_FENCE);

    setOutput(global_id, temp[local_id]);
}

__kernel void add_groups(
    __global int* data,
    __global int* sums)
{
    int global_id = get_global_id(0);
    int group_id = get_group_id(0);

    data[global_id] = data[global_id] + sums[group_id];
}

__kernel void add_groups_n(
    __global int* data,
    __global int* sums,
    __global int* length)
{
    int global_id = get_global_id(0);
    int group_id = get_group_id(0);

    if(global_id < *length)
        data[global_id] = data[global_id] + sums[group_id];
}