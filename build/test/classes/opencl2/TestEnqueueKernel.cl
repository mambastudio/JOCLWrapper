__kernel void my_func_A(global int *a, global int *b, global int *c)
{
    
}

__kernel void my_func_B(global int *a, global int *b, global int *c)
{
    long globalSize = 16;
    long localSize = 2;

    ndrange_t ndrange = ndrange_1D( global_work_size,
                                    local_work_size);
    // build ndrange information
    

    // example - enqueue a kernel as a block
    //enqueue_kernel(get_default_queue(), ndrange,
    //               ^{my_func_A(a, b, c);});

    
}
