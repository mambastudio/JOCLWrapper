__kernel void InitArrayIntZero(
    global int* array,
    global long* length)
{
    uint global_id = get_global_id(0);
    if(global_id < *length)
        array[global_id] = 0;    
}

__kernel void InitArrayIntOne(
    global int* array,
    global long* length)
{
    uint global_id = get_global_id(0);
    if(global_id < *length)
        array[global_id] = 1; 
}

__kernel void InitArrayIntIndex(
    global int* array,
    global long* length)
{
    uint global_id = get_global_id(0);
    if(global_id < *length)
        array[global_id] = (int)(global_id); 
}

__kernel void InitArrayIntIndexReverse(
    global int* array,
    global long* length)
{
    uint global_id = get_global_id(0);
    if(global_id < *length)
        array[global_id] = (int)(*length - global_id); 
}

__kernel void InitArrayIntOneNegative(
    global int* array,
    global long* length)
{
    uint global_id = get_global_id(0);
    if(global_id < *length)
        array[global_id] = -1; 
}

__kernel void TransformIntToOne(
    global int*     array,
    global int*     result,
    global long*    length)
{
    uint global_id = get_global_id(0);
    if(global_id >= *length)
        return;

    result[global_id] = select(0, 1, array[global_id] != 0);
}

__kernel void TransformIntToOneReverse(
    global int*     array,
    global int*     result,
    global long*    length)
{
    uint global_id = get_global_id(0);
    if(global_id >= *length)
        return;

    result[global_id] = select(1, 0, array[global_id] != 0);
}


int GET_INT(__global int* array, int index, long arbitraryLength)
{
    return select(0, array[index], index < arbitraryLength);   //b : a ? condition
}

int SET_INT(__global int* array, int value, int index, long arbitraryLength)
{
    array[index] = select(0, value, index < arbitraryLength);    //b : a ? condition
}

//sequential exclusive reduce on global scale (quite fast and trivial, for small global array size) global = 1, local = 1
__kernel void ReduceLoopInt (__global int* groupSum,                             
                             __global long* groupSize,
                             __global int* result)
{
    *result = 0;
    for(int i = 0; i < *groupSize; i++)
        *result += groupSum[i];
}

//https://dournac.org/info/gpu_sum_reduction
 __kernel void ReduceInt(__global const int     *input, 
                         __global       int     *groupSums,
                         __global       long    *arbitraryLength,
                         __local        int     *localSums)
{
    uint local_id  =  get_local_id(0);
    uint global_id = get_global_id(0);
    uint group_id = get_group_id(0);
    uint group_size  =  get_local_size(0);

    // Copy from global to local memory
    localSums[local_id]  =  input[global_id] ;

    //  Loop for computing localSums: divide WorkGroup into 2 parts
    for  ( uint stride  =  group_size/2; stride>0; stride /=2)
    {
        // Waiting for each 2x2 addition into given workgroup
        barrier ( CLK_LOCAL_MEM_FENCE ) ;

        // Add elements 2 by 2 between local_id and local_id + stride
        if  ( local_id  <  stride )
            localSums[local_id] += localSums[local_id  +  stride];
    }

    // Write result into partialSums[nWorkGroups] - DON'T USE SELECT!
    if( local_id == 0)
        groupSums[group_id] = localSums[0];
}   

//This can be replaced by any other prefix sum that is deemed suitable (works in local size only)
void BlellochInt(__global const int* in, __global int* out, __global int* groupSum, __global long* arbitraryLength, __local int* aux)
{
    int idl  = get_local_id(0); // index in workgroup
    int idg  = get_global_id(0);
    int idgr = get_group_id(0);
    int lSize = get_local_size(0);

    aux[idl] = GET_INT(in, idg, *arbitraryLength);
    barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);  //read to local first

    uint depth = 1;

    //upsweep
    for (uint stride = lSize >> 1; stride > 0; stride >>= 1) {
        barrier(CLK_LOCAL_MEM_FENCE);

        if (idl < stride) {
            uint i = depth * (2 * idl + 1) - 1;
            uint j = depth * (2 * idl + 2) - 1;
            aux[j] += aux[i];
        }

        depth <<= 1;
    }

    if (idl == 0)       
    { 
        groupSum[idgr] = aux[lSize - 1];
        aux[lSize - 1] = 0;
    }
    
    //downsweep
    for (uint stride = 1; stride < lSize; stride <<= 1) {

        depth >>= 1;
        barrier(CLK_LOCAL_MEM_FENCE);

        if (idl < stride) {
            uint i = depth * (2 * idl + 1) - 1;
            uint j = depth * (2 * idl + 2) - 1;

            int t = aux[j];
            aux[j] += aux[i];
            aux[i] = t;
        }
    }

    barrier(CLK_LOCAL_MEM_FENCE);

    //read back to global memory
    SET_INT(out, aux[idl], idg, *arbitraryLength);
}


//arbitrary length means not confined to the power of 2
__kernel void LocalScanInteger(__global const int* in, __global int* out, __global int* groupSum, __global long* arbitraryLength, __local int* aux)
{
    BlellochInt(in, out, groupSum, arbitraryLength, aux);
}

//sequential exclusive prefix sum on global scale (quite fast and trivial) global = 1, local = 1
__kernel void GroupScanInteger(__global int* groupSum,                             
                             __global long* groupSize)
{
    int accumulator = 0;
    int temp;
    for(int i = 0; i<*groupSize; i++)  
    {        
        temp = groupSum[i];
        groupSum[i] = accumulator;
        accumulator += temp;
    }
}

//do total scan (transfer local scan to global scan)
__kernel void LocalScanToGlobalInteger(__global int* out, __global int* groupSum, __global long* arbitraryLength)
{
      int idgr = get_group_id(0);
      int idg  = get_global_id(0);
      
      int value = GET_INT(out, idg, *arbitraryLength);
      int sum   = groupSum[idgr] + value;
      SET_INT(out, sum, idg, *arbitraryLength);
}

//Section for sorting
int Get(int index, long length, global int* data)
{   
    return select(data[index], INT_MAX, index >= length);
}

bool IsGreaterThan(int posStart, int posEnd, long length, global int* data)
{
    int value1 = Get(posStart, length, data);
    int value2 = Get(posEnd, length, data);

    return value1 > value2;
}

void Swap(int PosSIndex, int PosEIndex, global int* data)
{       
    int tmp          = data[PosSIndex];
    data[PosSIndex]  = data[PosEIndex];
    data[PosEIndex]  = tmp;
}

__kernel void Butterfly1_Int(global int* data, global long* lengthSize, global float* powerX)
{
     int gid = get_global_id(0);

     int t = gid;
     int radix = 2;
     long length = lengthSize[0];
     int PowerX = powerX[0];

     int yIndex      = (int) (t/(PowerX/radix));
     int kIndex      = (int) (t%(PowerX/radix));
     int PosStart    = (int) (kIndex + yIndex * PowerX);
     int PosEnd      = (int) (PowerX - kIndex - 1 + yIndex * PowerX);

     if(IsGreaterThan(PosStart, PosEnd, length, data))
         Swap(PosStart, PosEnd, data);
}

__kernel void Butterfly2_Int(global int* data, global long* lengthSize, global float* powerX)
{
    int gid = get_global_id(0);

    int t = gid;
    int radix = 2;
    long length = lengthSize[0];
    int PowerX = powerX[0];

    int yIndex      = (int) (t/(PowerX/radix));
    int kIndex      = (int) (t%(PowerX/radix));
    int PosStart    = (int) (kIndex + yIndex * PowerX);
    int PosEnd      = (int) (kIndex + yIndex * PowerX + PowerX/radix);
                            
    if(IsGreaterThan(PosStart, PosEnd, length, data))
        Swap(PosStart, PosEnd, data);
}
