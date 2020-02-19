<<<<<<< HEAD
void printInt(int i, bool newLine)
{
    if(newLine)
        printf("%2d\n", i);
    else
        printf("%2d  ", i);
}

kernel void sortbit(global int* values,
                    global int* values0,
                    global int* values1,
                    global int* insertIndex0,
                    global int* insertIndex1)
{
    int id = get_global_id( 0 );
    int value = values[id];

    if(!(value))
        values0[atomic_inc(insertIndex0)] = value;
     else
        values1[atomic_inc(insertIndex1)] = value;

}

kernel void concatenate(global int* values,
                        global int* values0,
                        global int* values1,
                        global int* insertIndex0,
                        global int* insertIndex1)
{
     int id = get_global_id( 0 );
     int index = insertIndex0[0];

     if(id < index)
        values[id] = values0[id];
     else
       values[id] = values1[id - index];


  
}
=======
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
>>>>>>> b459a896fc39f93a260df5ca672038ef50dd07f6
