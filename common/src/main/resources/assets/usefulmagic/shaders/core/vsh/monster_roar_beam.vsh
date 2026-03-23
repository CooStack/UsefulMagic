#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform float coneLength = 1.0;
uniform float coneRadius = 1.0;

out vec3 localCoord;

void main() {
    localCoord = position;
    float axial = clamp(position.y, 0.0, 1.0);
    float radius = coneRadius * axial;
    vec3 local = vec3(position.x * radius, axial * coneLength, position.z * radius);
    vec4 worldPos = modelMatrix * vec4(local, 1.0);
    gl_Position = projMatrix * viewMatrix * worldPos;
}
