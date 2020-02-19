__kernel void BitPredicate(__global int* predicate,
                           __global int* bitPredicate,
                           __global int* actualLength)
{
    int global_id             = get_global_id (0);
    int local_id              = get_local_id (0);
    int group_id              = get_group_id(0);
    
    global int* bitPredicateValue = bitPredicate + group_id;
    if(global_id<*actualLength)
        if(predicate[global_id])
            atomic_or(bitPredicateValue, 1 << local_id);
}

//count
__kernel void Count(__global int *bitPredicate,
                    __global int *procCount)
{
    int global_id             = get_global_id (0);
    procCount[global_id]      = popcount(bitPredicate[global_id]);
}

__kernel void CompactSIMD(__global int* predicate,
                          __global int* bitPredicate,
                          __global int* procOffset,
                          __global int* output,
                          __global int* actualLength)
{
    int global_id             = get_global_id (0);
    int local_id              = get_local_id (0);
    int group_size            = get_local_size(0);
    int group_id              = get_group_id(0);

    if(global_id < *actualLength)
      if(predicate[global_id])
      {
           int m1 = bitPredicate[group_id] & ((1 << local_id) - 1);
           int s1 = popcount(m1);
           output[procOffset[group_id] + s1] = predicate[global_id];
      }
}