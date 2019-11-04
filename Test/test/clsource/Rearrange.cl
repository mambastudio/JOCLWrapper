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
