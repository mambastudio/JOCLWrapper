// bounding box
typedef struct
{
   float4 minimum;
   float4 maximum;

}BoundingBox;

void addToBoundingBox(BoundingBox* bound, float4 point)
{
   bound->minimum = min(bound->minimum, point);
   bound->maximum = max(bound->maximum, point);
}

float4 boundingBoxExtents(BoundingBox bound)
{     
   return bound.maximum - bound.minimum;
}

// print float value
void printFloat(float v)
{
    printf("%4.2f\n", v);
}

void printFloatStr(char* c, float v)
{
    printf("%s %4.2f\n", c, v);
}

// print float2
void printFloat2(float2 v)
{
    printf("%4.2v2f\n", v);
}

// print float4
void printFloat4(float4 v)
{
    printf("%4.2v4f\n", v);
}

void printInt4(int4 v)
{
    printf("%v4d\n", v);
}

void printBound(BoundingBox bound)
{
    printf("[%4.2v4f] [%4.2v4f]\n", bound.minimum, bound.maximum);
}

//Global data of the grid
typedef struct {
    int4            grid_dims;
    BoundingBox     grid_bbox;
    float4          cell_size;        
    int             grid_shift;
}Hagrid;

typedef struct{
    float4  position;
    float4  lookat;    
    float4  up;
    float2  dimension;
    float   fov;
}Camera;

__kernel void Test(global Hagrid*       hagrid)
{
    int id = get_global_id(0);

    if(id == 0)
    {
        printf("Hagrid");
        printf("--------------------------------");
        printf("grid_dims      :%v4d", hagrid->grid_dims);
        printf("grid_bbox      :[%4.2v4f] [%4.2v4f]", hagrid->grid_bbox.minimum, hagrid->grid_bbox.maximum);
        printf("grid cell size :%4.2v4f\n", hagrid->cell_size);
        printf("grid_dims      :%d", hagrid->grid_shift);      

        printFloat4(boundingBoxExtents(hagrid->grid_bbox));  
    }
}

__kernel void TestCamera(global Camera*       cameraGlobal)
{
    Camera camera = *cameraGlobal;
    printFloat4(camera.position);
    printFloat4(camera.lookat);
    printFloat4(camera.up);
    printFloat2(camera.dimension);
    printFloat(camera.fov);
   
    printFloat(camera.dimension.s0);
}
