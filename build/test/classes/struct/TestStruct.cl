typedef struct
{
    int n;
    float arr[5];
}SimpleStruct;

__kernel void testStruct(__global SimpleStruct* simpleStruct)
{
    simpleStruct->arr[4] = 32;
    simpleStruct->n = 3;  
}