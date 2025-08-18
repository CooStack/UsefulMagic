#version 330


layout (location = 0) in vec3 pos;
uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;
uniform float u_time;
uniform float u_radius;

void main() {
    float origin = length(pos);
    float pulse = (u_radius - 5.); // 0.1-1.0之间脉动
    if (pulse < 0.) {
        pulse = 0.;
    }
//    vec2 xz = pos.xz * pulse;
    //    gl_Position = projMat * viewMat * transMat * vec4(xz.x, pos.y, xz.y, 1.);
    gl_Position = projMat * viewMat * transMat * vec4(pos * pulse, 1.);
}