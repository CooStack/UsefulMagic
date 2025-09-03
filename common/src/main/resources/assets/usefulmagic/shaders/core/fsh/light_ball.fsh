#version 330 core
out vec4 FragColor;

in vec3 point;
in vec3 normal;

uniform vec3 camera;
uniform vec3 camera_view;
uniform vec3 glowColor = vec3(0.4, 0.6, 1.0);
uniform float glowIntensity = 2.0;
uniform float glowFalloff = 0.1;
uniform float emptyAlpha = .1;

void main() {
    // 计算观察方向
    vec3 viewDir = normalize(camera - point);
    float fresnel = dot(normal, viewDir);
    float progress = smoothstep(0.0, 0.5, pow(fresnel, 4.));
    FragColor = mix(vec4(glowColor * glowIntensity, 1.), vec4(glowColor * glowFalloff, emptyAlpha), progress);
}