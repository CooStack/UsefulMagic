#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform float beamRadius = 0.1;
uniform float beamLength = 1.0;
uniform float coneEndRatio = 0.1;

out vec3 beamCoord;

void main() {
    float coneScale = smoothstep(0.0, coneEndRatio, position.y);
    beamCoord = vec3(position.x * coneScale, position.y, position.z * coneScale);
    vec3 local = vec3(beamCoord.x * beamRadius, beamCoord.y * beamLength, beamCoord.z * beamRadius);
    vec4 worldPos = modelMatrix * vec4(local, 1.0);
    gl_Position = projMatrix * viewMatrix * worldPos;
}
