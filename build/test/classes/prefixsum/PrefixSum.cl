int GET_INT(__global int* array, int index, int arbitraryLength)
{
    if(index < arbitraryLength)
       return array[index];
    else
       return 0;
}

int SET_INT(__global int* array, int value, int index, int arbitraryLength)
{
    if(index < arbitraryLength)
       array[index] = value;
}



//This can be replaced by any other prefix sum that is deemed suitable (works in local size only)
void koggeStoneInt(__global const int* in, __global int* out, __global int* groupSum, __global int* arbitraryLength, __local int* aux)
{
     int idl  = get_local_id(0); // index in workgroup
     int idg  = get_global_id(0);
     int idgr = get_group_id(0);
     int lSize = get_local_size(0);

     aux[idl] = GET_INT(in, idg, *arbitraryLength);
     barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);  //read to local first

     for(int offset = 1; offset < lSize; offset *= 2)
     {
          private int temp;
          if(idl >= offset) temp = aux[idl - offset];
          barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);
          if(idl >= offset) aux[idl] = temp + aux[idl];
          barrier(CLK_LOCAL_MEM_FENCE|CLK_GLOBAL_MEM_FENCE);

     }

     if(idl == (lSize-1))
       groupSum[idgr] = aux[idl];

     if(aux[idl] > 0)
       SET_INT(out, aux[idl - 1], idg, *arbitraryLength);
     else
       SET_INT(out, aux[0], idg, *arbitraryLength);
}

//arbitrary length means not confined to the power of 2
__kernel void localScanInteger(__global const int* in, __global int* out, __global int* groupSum, __global int* arbitraryLength, __local int* aux)
{
     koggeStoneInt(in, out, groupSum, arbitraryLength, aux);
}

//sequential prefix sum on global scale (quite fast and trivial) global = 1, local = 1
__kernel void groupScanInteger(__global int* groupSum,
                             __global int* groupPrefixSum,
                             __global int* groupSize)
{
      for(int i = 1; i<*groupSize; i++)
      {
          groupPrefixSum[i] = 0;
          groupPrefixSum[i] = groupPrefixSum[i-1] + groupSum[i-1];
      }
}

//do total scan (transfer local scan to global scan)
__kernel void globalScanInteger(__global int* out, __global int* groupSum, __global int* arbitraryLength)
{
      int idgr = get_group_id(0);
      int idg  = get_global_id(0);
      
      int value = GET_INT(out, idg, arbitraryLength);
      int sum   = groupSum[idgr] + value;
      SET_INT(out, sum, idg, arbitraryLength);
}

/**
 *  CIntBuffer predicate        = configuration.allocInt("predicate", leafS, READ_WRITE);
 *  CIntBuffer localscan        = configuration.allocInt("localscan", leafS, READ_WRITE);   //like koggeStone above
 *  CIntBuffer groupsum         = configuration.allocInt("groupsum" , getNumOfGroups(leafS, LOCALSIZE), READ_WRITE);
 *  CIntBuffer groupprefixsum   = configuration.allocInt("groupprefixsum", getNumOfGroups(leafS, LOCALSIZE), READ_WRITE);
 *  CIntBuffer groupsize        = configuration.allocIntValue("groupsize", getNumOfGroups(leafS, LOCALSIZE), READ_WRITE);
 *  CIntBuffer localsize        = configuration.allocIntValue("localsize", LOCALSIZE, READ_WRITE);
 *  CIntBuffer compactlength    = configuration.allocIntValue("compactlength", leafS, READ_WRITE);
 * 
 *  CKernel groupPrefixSumKernel        = configuration.createKernel("groupScan", predicate, groupsum, groupprefixsum, localscan, groupsize, localsize, compactlength);
 */


