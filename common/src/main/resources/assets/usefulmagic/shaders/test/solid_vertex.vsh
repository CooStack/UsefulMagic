#version 330


layout (location = 0) in vec3 pos;
uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;
uniform float u_time;
void main(){
    float pulse = abs(sin(u_time)) * 0.99 + 0.01; // 0.1-1.0之间脉动
    gl_Position = projMat * viewMat * transMat * vec4(pos * pulse, 1.);
}