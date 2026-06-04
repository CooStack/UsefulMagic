#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform mat3 inverseViewRotationMatrix;
uniform vec3 centerOffset = vec3(0.0);
uniform vec2 scale = vec2(1.0);
uniform float spin = 0.0;
uniform float depthOffset = 0.0;
uniform vec3 segmentStart = vec3(0.0);
uniform vec3 segmentEnd = vec3(0.0);
uniform float segmentWidth = 0.1;
uniform float mode = 0.0;

out vec2 vUv;
out vec2 vLocal;

vec2 rotate2d(vec2 value, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c) * value;
}

void main() {
    vec2 local = position.xy;
    vUv = local * 0.5 + 0.5;
    vLocal = local;

    vec3 finalOffset;
    if (mode > 0.5) {
        vec3 tangent = segmentEnd - segmentStart;
        float tangentLength = length(tangent);
        if (tangentLength <= 0.0001) {
            tangent = vec3(1.0, 0.0, 0.0);
        } else {
            tangent /= tangentLength;
        }
        vec3 cameraRight = vec3(inverseViewRotationMatrix[0][0], inverseViewRotationMatrix[0][1], inverseViewRotationMatrix[0][2]);
        vec3 cameraUp = vec3(inverseViewRotationMatrix[1][0], inverseViewRotationMatrix[1][1], inverseViewRotationMatrix[1][2]);
        vec3 viewFacing = normalize(cross(cameraRight, cameraUp));
        vec3 side = cross(viewFacing, tangent);
        if (length(side) <= 0.0001) {
            side = cameraUp;
        } else {
            side = normalize(side);
        }
        vec3 along = mix(segmentStart, segmentEnd, vUv.x);
        finalOffset = along + side * local.y * segmentWidth;
    } else {
        vec2 rotated = rotate2d(local, spin);
        finalOffset = centerOffset + inverseViewRotationMatrix * vec3(rotated * scale, depthOffset);
    }

    vec4 worldPos = modelMatrix * vec4(finalOffset, 1.0);
    gl_Position = projMatrix * viewMatrix * worldPos;
}
