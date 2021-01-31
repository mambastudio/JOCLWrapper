typedef struct
{
    int charge;
} Atom;

typedef struct
{
    float mass;
    float4 position;
    Atom atom;
    int binary[3];
    float4 velocity;

} Particle;
 
__kernel void test(__global Particle *particles)
{
    int gid = get_global_id(0);

    particles[gid].mass     += 1;
    particles[gid].position  = 4;
    particles[gid].velocity  = gid + 1;
    particles[gid].atom.charge *= 34534;
    particles[gid].binary[1] = 3;
    particles[gid].binary[0] = 3;
}
