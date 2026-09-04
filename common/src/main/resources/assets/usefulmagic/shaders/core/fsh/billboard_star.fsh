#version 330 core

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 MaskColor;

in vec2 vLocalUv;

uniform vec3 color = vec3(1.0, 0.94, 0.72);
uniform float alpha = 0.35;
uniform float brightness = 1.2;
uniform float haloStrength = 0.4;
uniform float rayStrength = 1.0;
uniform float raySharpness = 0.7;
uniform float coreRadius = 0.34;
uniform float whiteCore = 0.6;
uniform float twinkle = 0.5;
uniform float collapse = 0.0;
uniform float time = 0.0;
uniform int renderTarget = 2;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void outputColor(vec4 value) {
    FragColor = renderTarget != 1 ? value : vec4(0.0);
    MaskColor = renderTarget != 0 ? value : vec4(0.0);
}

void main() {
    vec2 uv = vLocalUv;
    float radius = length(uv);
    float angle = atan(uv.y, uv.x);

    float starSpike = pow(abs(cos(angle * 2.0)), mix(1.8, 11.0, saturate(raySharpness)));
    starSpike = mix(0.12, 1.0, starSpike);

    float pulse = mix(0.88, 1.18, saturate(twinkle));
    float starRadius = mix(coreRadius + 0.05, 1.08 + twinkle * 0.10, starSpike * saturate(rayStrength));
    float feather = mix(0.26, 0.08, saturate(raySharpness));
    float starMask = 1.0 - smoothstep(starRadius, starRadius + feather, radius);

    float halo = exp(-radius * radius * mix(2.8, 4.2, saturate(collapse)));
    halo *= haloStrength * pulse;

    float softCore = exp(-radius * radius * mix(6.0, 11.0, saturate(whiteCore)));
    float hardCore = exp(-radius * radius * mix(16.0, 34.0, saturate(whiteCore)));
    float shimmer = 0.92 + 0.08 * sin(time * 1.05 + angle * 4.0 + radius * 9.0);

    float finalAlpha = starMask * (0.78 + softCore * 0.28) + halo * 0.18;
    finalAlpha *= alpha * shimmer * (1.0 - collapse * 0.42);
    finalAlpha = saturate(finalAlpha);
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 shellColor = mix(color * vec3(0.86, 0.88, 1.0), color, 0.74 + starSpike * 0.18);
    vec3 warmCore = mix(shellColor, vec3(1.0, 0.98, 0.92), saturate(softCore * 0.68 + hardCore * 0.72));
    vec3 haloColor = mix(shellColor, vec3(1.0, 0.96, 0.82), saturate(halo * 0.24));

    vec3 finalColor = haloColor * (0.28 + halo * 0.54);
    finalColor += warmCore * (starMask * 1.26 + softCore * 0.54 + hardCore * 0.48);
    finalColor *= brightness;

    outputColor(vec4(finalColor, finalAlpha));
}
