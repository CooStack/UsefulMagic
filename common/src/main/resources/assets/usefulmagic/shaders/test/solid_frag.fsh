#version 330


out vec4 FragColor;

uniform float u_time;
void main() {
    FragColor = vec4(abs(sin(u_time)), 0.8, 1., 1.);
}