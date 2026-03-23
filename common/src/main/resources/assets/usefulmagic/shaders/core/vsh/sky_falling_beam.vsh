#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform vec3 scale = vec3(1.0);
uniform float offsetY = 0.0;

out vec3 localPos;

void main() {
    localPos = position;
    vec3 scaled = vec3(position.x * scale.x, position.y * scale.y + offsetY, position.z * scale.z);
    vec4 worldPos = modelMatrix * vec4(scaled, 1.0);
    gl_Position = projMatrix * viewMatrix * worldPos;
}
