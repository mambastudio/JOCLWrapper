/// Voxel map entry
typedef struct{    
    int log_dim;    ///< Logarithm of the dimensions of the entry (0 for leaves)
    int begin;      ///< Next entry index (cell index for leaves)
} Entry;

void printInt4(int4 v)
{
   printf("%d, %d, %d, %d\n", v.x, v.y, v.z, v.w);
}

__kernel void testStruct(__global Entry* entries)
{       
    int4* ptr = (int4*)(entries + 0);
}