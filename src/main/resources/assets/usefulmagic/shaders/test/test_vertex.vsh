#version 330 core
uniform mat4 transform;
uniform mat4 ModelViewMat;
layout (location = 0) in vec3 pos;

void main() {
    gl_Position = ModelViewMat * transform  * vec4(pos, 1.);
}