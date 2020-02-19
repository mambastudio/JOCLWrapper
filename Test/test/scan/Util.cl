kernel void predicateFloat(global float* array, global int* predicates)
{
     uint global_id = get_global_id ( 0 );
     global float* value = array + global_id;
     global int* predicate = predicates + global_id;
     
     if(*value > 0)
         *predicate = 1;
     else
         *predicate = 0;
  
}