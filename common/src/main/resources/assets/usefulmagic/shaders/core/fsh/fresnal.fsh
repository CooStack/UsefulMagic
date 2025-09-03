#version 330 core
out vec4 FragColor;
out vec4 BrightColor;
in vec3 point;
in vec3 normal;

uniform vec3 camera;
uniform vec3 camera_view;
uniform vec3 glowColor = vec3(0.4, 0.6, 1.0);
uniform float glowIntensity = 2.0;
uniform float glowFalloff = 5.0;
uniform float fsl = 0.3;// 折射率

void main() {
    // 计算观察方向
    vec3 viewDir = normalize(camera - point);
    // 计算法线和观察方向的点积
    float theta = dot(normal, viewDir); // cos
    float fresnel = 1. - abs(theta);
    float intensity = fsl + (1. - fsl) * pow(fresnel, glowFalloff);
    // 输出颜色
    vec4 color = mix(vec4(glowColor, 1.) * glowIntensity, vec4(0.), 1. - intensity);
    FragColor = color;
    if (dot(color.rgb, vec3(0.2126, 0.7152, 0.0722)) > 0.7) {
        BrightColor = color;
    }
}