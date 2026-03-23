#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform vec3 radius = vec3(1.0);
uniform float time = 0.0;
uniform float deployScale = 1.0;
uniform float collapseProgress = 0.0;
uniform float disturbanceStrength = 0.35;
uniform int passMode = 0;

out vec3 vLocalDir;
out vec3 vViewPos;
out float vWave;
out float vFineWave;

float shieldWave(vec3 dir) {
    float a = sin(dir.x * 4.6 + dir.y * 2.7 + time * 0.31);
    float b = sin(dir.z * 5.8 - dir.x * 3.4 - time * 0.27);
    float c = sin((dir.x - dir.y + dir.z) * 8.9 + time * 0.43);
    return a * 0.46 + b * 0.34 + c * 0.20;
}

float fineWave(vec3 dir) {
    float a = sin((dir.x + dir.z) * 15.0 + time * 0.72);
    float b = sin((dir.y - dir.z) * 18.0 - time * 0.66);
    return a * 0.55 + b * 0.45;
}

void main() {
    vec3 dir = normalize(position);
    float live = clamp(deployScale, 0.0, 1.0);
    float wave = shieldWave(dir);
    float fine = fineWave(dir);
    float calmAmplitude = (0.006 + disturbanceStrength * 0.012) * live * (1.0 - collapseProgress * 0.20);
    float releaseAmplitude = collapseProgress * 0.044;
    float glowLift = passMode == 1 ? 0.012 : 0.0;
    float shellScale = live * (1.0 + wave * (calmAmplitude + releaseAmplitude) + fine * calmAmplitude * 0.35 + glowLift);
    vec3 localPos = dir * radius * shellScale;
    vec4 worldPos = modelMatrix * vec4(localPos, 1.0);
    vec4 viewPos = viewMatrix * worldPos;

    gl_Position = projMatrix * viewPos;
    vLocalDir = dir;
    vViewPos = viewPos.xyz;
    vWave = wave;
    vFineWave = fine;
}
