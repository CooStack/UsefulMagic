#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform mat3 inverseViewRotationMatrix;
uniform vec2 scale = vec2(1.0);
uniform float spin = 0.0;
uniform float depthOffset = 0.0;

out vec2 vLocalUv;

vec2 rotate2d(vec2 value, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c) * value;
}

void main() {
    vec2 rotated = rotate2d(position.xy, spin);
    vec3 billboardOffset = inverseViewRotationMatrix * vec3(rotated * scale, depthOffset);
    vec4 worldPos = modelMatrix * vec4(billboardOffset, 1.0);
    vLocalUv = rotated;
    gl_Position = projMatrix * viewMatrix * worldPos;
}
