// print int
void printInt(int i, bool newLine)
{
    if(newLine)
        printf("%2d\n", i);
    else
        printf("%2d  ", i);
}

kernel void sortBit(global int* values,
                    global int* bit,
                    global int* values0,
                    global int* values1,
                    global int* insertIndex0,
                    global int* insertIndex1)
{
     int id = get_global_id( 0 );
     int mask = 0x00000001 << bit[0];
     int value = values[id];

     if(!(value & mask))
        values0[atomic_inc(insertIndex0)] = value;
     else
        values1[atomic_inc(insertIndex1)] = value;
        
        //printInt(insertIndex1[0], true);
}

kernel void insertBit(global int* values,
                      global int* bit,
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
     {
       values[id] = values1[id - index];

     }
         
      printInt(id, true);
}
