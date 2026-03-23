#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform float beamRadius = 0.1;
uniform float beamLength = 1.0;

out vec3 beamCoord;

void main() {
    beamCoord = position;
    vec3 local = vec3(position.x * beamRadius, position.y * beamLength, position.z * beamRadius);
    vec4 worldPos = modelMatrix * vec4(local, 1.0);
    gl_Position = projMatrix * viewMatrix * worldPos;
}
