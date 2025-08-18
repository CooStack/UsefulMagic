#version 330 core
out vec4 FragColor;
uniform float u_time;
uniform float u_radius;

in vec3 fragPos;
in vec3 normal;
in float edgeFactor; // 来自顶点着色器的边缘因子

void main() {
    // 基础颜色（随时间变化）
    vec3 baseColor = vec3(0.5, 0.5, 1.);

    // 菲涅尔效应（Fresnel）创建光晕
    vec3 viewDir = normalize(-fragPos); // 视图方向
    float fresnel = dot(normal, viewDir);
    fresnel = pow(1.0 - fresnel, 2.0); // 加强边缘效果

    // 光晕颜色（比基础色更亮）
    vec3 glowColor = baseColor * 2.0;

    // 混合基础色和光晕色
    vec3 finalColor = mix(baseColor, glowColor, fresnel);

    // 透明度（随时间变化，边缘更透明）
    float alpha = 0.7;
    alpha *= smoothstep(0.0, 0.5, fresnel); // 边缘透明度更高

    FragColor = vec4(finalColor, alpha);
}