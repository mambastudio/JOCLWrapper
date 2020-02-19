// print int
void printInt(int i, bool newLine)
{
    if(newLine)
        printf("%2d\n", i);
    else
        printf("%2d  ", i);
}

void printlnInt(int i)
{
    printInt(i, true);
}

int upsweep(__local int * temp, int offset, int n)
{
    int lid = get_local_id(0);
    for (int d = n >> 1; d > 0; d >>= 1) {
        barrier(CLK_LOCAL_MEM_FENCE);
        if (lid < d) {
            int ai = offset * (2 * lid + 1) - 1;
            int bi = offset * (2 * lid + 2) - 1;

            temp[bi] += temp[ai];
        }
        offset <<= 1;
    }
    return offset;
}

void downsweep(__local int * temp, int offset, int n)
{
	int lid = get_local_id(0);
	for (int d = 1; d < n; d <<= 1) {
        offset >>= 1;

        barrier(CLK_LOCAL_MEM_FENCE);

        if (lid < d) {
            int ai = offset * (2 * lid + 1) - 1;
            int bi = offset * (2 * lid + 2) - 1;

            int t = temp[ai];
            temp[ai] = temp[bi];
            temp[bi] += t;
        }
    }
}

__kernel void blelloch_scan(__global const int* input,
                            __global int*  output,
                            __local  int  temp[4])
{
    // WORKSPACE
    int local_size = get_local_size(0);
    int global_size = get_global_size(0);

    
    // ORIENTATION 
    int group_id= get_group_id(0);
    int thread_id = get_local_id(0);
    
    int group_offset = group_id * local_size;
   
   // printf("group_id: %d", group_id);

    // STORE VALUES IN LOCAL MEMORY^
    temp[thread_id] = input[group_offset + thread_id];
    temp[thread_id + 1] = input[group_offset + thread_id + 1];

    // UPSWEEP (=reduce phase)
    int offset = upsweep(temp, 1, local_size);
    // printf("offset after reduce: %d", offset);
    
    barrier(CLK_LOCAL_MEM_FENCE);
    if (thread_id == 0) {
        // store last value of block in blocksums array!
        //blocksums[group_id] = temp[local_size - 1];
        // exclusive scan ()=> last thread sets last index to zero
        temp[local_size - 1] = 0;
    }

    // DOWNSWEEP PHASE
    downsweep(temp, offset, local_size);
    
    // der letzte dreht das licht ab
    barrier(CLK_LOCAL_MEM_FENCE);
    output[group_offset + thread_id] = temp[thread_id];
    output[group_offset + thread_id + 1] = temp[thread_id + 1];
}