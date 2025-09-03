#version 330 core

layout (location = 0) in vec3 pos;

uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;

out vec3 point;
out vec3 normal;

void main() {
    vec4 world_pos = transMat * vec4(pos, 1.);
    point = world_pos.xyz;
    normal = normalize(vec3(pos.x, 0., pos.z));
    gl_Position = projMat * viewMat * world_pos;;
}