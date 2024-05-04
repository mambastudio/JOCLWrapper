// Function to get the last field of a float or float4
inline float GetLastFieldOfFloat(float val) {
    return val;
}

inline float GetLastFieldOfFloat4(float4 val) {
    return val.w;
}

// Function to perform prefix sum on float4
inline void PrefixSumComponentsFloat4(float4* val) {
    val->y += val->x;
    val->z += val->y;
    val->w += val->z;
}

// Function to perform the operation for exclusive prefix sum
inline void ApplyExclusivePrefixSum(float* value, float originalValue) {
    // To obtain the exclusive prefix-sum from the inclusive one, we subtract the input value
    *value -= originalValue;
}

// Function to perform the operation for inclusive prefix sum
inline void ApplyInclusivePrefixSum(float* value) {
    // The algorithm natively computes the inclusive sum. Nothing to do here.
}

#define LOCAL_SIZE 16
// Compute the parallel prefix-sum of a work group for float4
inline void PrefixSumBlockFloat4(float4* val, float* total) {

    __local float smem[LOCAL_SIZE];

    // Prepare the prefix sum within the n components in float4.
    PrefixSumComponentsFloat4(val);

    for (int i = 1; i < get_local_size(0); i <<= 1) {
        smem[get_local_id(0)] = GetLastFieldOfFloat4(*val);
        barrier(CLK_LOCAL_MEM_FENCE);

        // Add the value to the local variable
        if (get_local_id(0) >= i) {
            const float offset = smem[get_local_id(0) - i];
            *val += offset;
        }
        barrier(CLK_LOCAL_MEM_FENCE);
    }

    // Apply prefix sum running total from the previous block
    *val += *total;
    barrier(CLK_LOCAL_MEM_FENCE);

    // Update the prefix sum running total using the last thread in the block
    if (get_local_id(0) == get_local_size(0) - 1) {
        *total = GetLastFieldOfFloat4(*val);
    }
    barrier(CLK_LOCAL_MEM_FENCE);

    // Apply the final transformation
    float tmp_w = val->w; // Store the value of val->w in a temporary variable
    ApplyInclusivePrefixSum(&tmp_w); // Apply the inclusive prefix sum operation on tmp_w
    val->w = tmp_w; // Assign the modified value back to val->w
}
