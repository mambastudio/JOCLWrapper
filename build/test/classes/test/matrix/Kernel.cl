kernel void execute(global float16* matrixA,
                    global float16* matrixB,
                    global float16* matrixC)
{
    printMat4f(getMatrixIdentity());
}
