#version 150 core

uniform float u_time;
uniform vec3 u_pos;
uniform vec3 u_camera;

void main(){
    float d = distance(u_pos, u_camera);
    float step = smoothstep(0., 1., d);
    vec4 ball = vec4(0.5, 0.5, 0.5, 1.);
}