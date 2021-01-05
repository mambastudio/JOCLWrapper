void printFloat(float v)
{
    printf("%4.12f\n", v);
}
__kernel void SetPredicate(__global int* input,
                           __global int* predicate,
                           __global int* actualLength)
{
    int global_id             = get_global_id (0);
    if(global_id < *actualLength)
    {
        if(input[global_id] > 0)
            predicate[global_id] = 1;
        else
            predicate[global_id] = 0;
    }
}

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

//sequential prefix sum (not much is being done here, hence a fast process)
__kernel void SequentialPrefixSum(__global int* bitPredicate,
                                  __global int *procCount,
                                  __global int* procOffset,
                                  __global int* size,
                                  __global int* compactLength)
{
      for(int i = 1; i<*size; i++)
          procOffset[i] = procOffset[i-1] + procCount[i-1];
          
      *compactLength = procOffset[*size - 1] + popcount(bitPredicate[*size - 1]);
      //printFloat(total);
}

//count
__kernel void Count(__global int *bitPredicate,
                    __global int *procCount)
{
    int global_id             = get_global_id (0);
    procCount[global_id]      = popcount(bitPredicate[global_id]);
}

__kernel void CompactSIMD(__global int* input,
                          __global int* predicate,
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
    {
      if(predicate[global_id])
      {
           int m1 = bitPredicate[group_id] & ((1 << local_id) - 1);
           int s1 = popcount(m1);
           output[procOffset[group_id] + s1] = input[global_id];
      }
    }
}