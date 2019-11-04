// print float value
void printFloat(float v)
{
    printf("%4.8f\n", v);
}

void printFloat3(float3 v)
{
    printf("%4.8v3f\n", v);
}

// print float4
void printFloat4(float4 v)
{
    printf("%4.8v4f\n", v);
}

// print int
void printInt(int i, bool newLine)
{
    if(newLine)
        printf("%2d\n", i);
    else
        printf("%2d  ", i);
}

void printInt2(int2 v)
{
   printf("%d, %d\n", v.x, v.y);
}

// print boolean
void printBoolean(bool value)
{
    printf(value ? "true \n" : "false \n");
}

void printMat4f(mat4 mat)
{
    printFloat4(getMatrixRow(mat, 0));
    printFloat4(getMatrixRow(mat, 1));
    printFloat4(getMatrixRow(mat, 2));
    printFloat4(getMatrixRow(mat, 3));
}

