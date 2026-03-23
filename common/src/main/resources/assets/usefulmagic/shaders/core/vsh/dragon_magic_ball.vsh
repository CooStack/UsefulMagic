#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform float scale = 1.0;
uniform float time = 0.0;
uniform float discardProgress = 0.0;
uniform int passMode = 0;

out vec3 vLocalDir;
out vec3 vViewPos;
out float vShellWave;

float shellWave(vec3 dir) {
    float a = sin(dir.x * 5.7 + dir.y * 2.3 + time * 0.52);
    float b = sin(dir.z * 6.4 - dir.x * 3.1 - time * 0.38);
    float c = sin((dir.x + dir.y - dir.z) * 10.8 + time * 0.27);
    return a * 0.44 + b * 0.34 + c * 0.22;
}

void main() {
    vec3 dir = normalize(position);
    float wave = shellWave(dir);
    float cloudLift = passMode == 1 ? 0.105 : 0.0;
    float bloomPull = passMode == 2 ? -0.018 : 0.0;
    float releaseLift = discardProgress * 0.035;
    float surfaceNoise = wave * (passMode == 1 ? 0.038 : 0.018);
    vec3 localPos = dir * scale * (1.0 + cloudLift + bloomPull + releaseLift + surfaceNoise);
    vec4 worldPos = modelMatrix * vec4(localPos, 1.0);
    vec4 viewPos = viewMatrix * worldPos;

    gl_Position = projMatrix * viewPos;
    vLocalDir = dir;
    vViewPos = viewPos.xyz;
    vShellWave = wave;
}
