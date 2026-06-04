#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform vec3 radius = vec3(1.0);
uniform float time = 0.0;
uniform float flowSpeed = 1.0;
uniform int passMode = 0;

out vec3 vLocalDir;
out vec3 vViewPos;
out vec3 vWorldPos;
out float vDisturbance;

float flameWave(vec3 dir) {
    float a = sin(dir.x * 7.1 + dir.y * 4.3 + time * flowSpeed * 0.42);
    float b = sin(dir.z * 8.4 - dir.x * 3.7 - time * flowSpeed * 0.36);
    float c = sin((dir.x + dir.y - dir.z) * 13.2 + time * flowSpeed * 0.58);
    return a * 0.42 + b * 0.34 + c * 0.24;
}

void main() {
    vec3 dir = normalize(position);
    float front = clamp(dir.y * 0.5 + 0.5, 0.0, 1.0);
    float wave = flameWave(dir);
    float lift = passMode == 2 ? 0.075 : 0.035;
    float lick = max(front - 0.18, 0.0) * (0.018 + lift * (0.5 + 0.5 * wave));
    vec3 localPos = dir * radius * (1.0 + lick);
    vec4 worldPos = modelMatrix * vec4(localPos, 1.0);
    vec4 viewPos = viewMatrix * worldPos;

    gl_Position = projMatrix * viewPos;
    vLocalDir = dir;
    vViewPos = viewPos.xyz;
    vWorldPos = worldPos.xyz;
    vDisturbance = wave;
}
