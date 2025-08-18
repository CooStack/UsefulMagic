#version 330 core

in vec2 tex_uv;

out vec4 FragColor;

uniform sampler2D buffered_texture;
void main() {
    vec4 color = texture(buffered_texture, tex_uv);
    FragColor = color;
}