#version 330 core

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 MaskColor;

in vec3 localCoord;

uniform vec3 color = vec3(1.0, 1.0, 1.0);
uniform float alpha = 0.28;
uniform float brightness = 1.6;
uniform float edgeBoost = 0.8;
uniform float coreGlow = 0.28;
uniform float refractionStrength = 0.14;
uniform float noiseStrength = 0.6;
uniform float lifetime = 18.0;
uniform float fadeTicks = 7.0;
uniform float time = 0.0;
uniform int renderTarget = 2;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void outputColor(vec4 value) {
    FragColor = renderTarget != 1 ? value : vec4(0.0);
    MaskColor = renderTarget != 0 ? value : vec4(0.0);
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float pulse(float value) {
    return 0.5 + 0.5 * sin(value);
}

float smoothBand(float value, float center, float width) {
    float halfWidth = max(width * 0.5, 0.001);
    return 1.0 - smoothstep(halfWidth * 0.36, halfWidth, abs(value - center));
}

void main() {
    float axial = saturate(localCoord.y);
    float radial = saturate(length(localCoord.xz));
    float angle = atan(localCoord.z, localCoord.x);
    float active = 1.0 - smoothstep(lifetime, lifetime + max(fadeTicks, 1.0), time);

    float axialNoise = pulse(axial * 15.0 - time * 1.6);
    axialNoise += pulse(angle * 7.0 + axial * 9.0 + time * 1.1) * 0.42;
    axialNoise += hash21(vec2(angle * 0.17 + time * 0.04, axial * 8.0)) * 0.36;
    axialNoise = mix(0.0, axialNoise - 0.54, noiseStrength);

    float refractedAxial = fract(axial - time * 0.105 + axialNoise * refractionStrength);
    float waveA = smoothBand(refractedAxial, 0.18, 0.105);
    float waveB = smoothBand(fract(refractedAxial + 0.34), 0.18, 0.095) * 0.76;
    float waveC = smoothBand(fract(refractedAxial + 0.66), 0.18, 0.075) * 0.54;
    float waveBands = waveA + waveB + waveC;

    float edgeNoise = pulse(angle * 6.0 + time * 2.3 + axial * 12.0);
    edgeNoise += pulse(angle * 13.0 - time * 1.7) * 0.34;
    edgeNoise += hash21(vec2(angle * 0.32, axial * 10.0 + time * 0.09)) * 0.26;
    edgeNoise = saturate(edgeNoise);

    float tornEdge = smoothstep(0.50 + edgeNoise * 0.10, 0.96 + edgeNoise * 0.08, radial);
    float edgeMask = pow(tornEdge, 1.55) * edgeBoost;
    float radialFade = pow(saturate(1.0 - radial), 1.35);
    float outerFalloff = pow(saturate(1.0 - radial * 0.88), 1.85);
    float distanceFade = pow(saturate(1.0 - axial * 0.12), 1.12);

    float throatGlow = exp(-radial * radial * mix(18.0, 6.2, axial)) * (1.0 - smoothstep(0.34, 0.92, axial));
    throatGlow *= coreGlow;
    float airWobble = pulse((axial + radial * 0.42) * 36.0 - time * 5.4 + edgeNoise * 2.2) * 0.12;

    float alphaMask = waveBands * (0.42 + outerFalloff * 0.28 + edgeMask * 0.42);
    alphaMask *= distanceFade;
    alphaMask += throatGlow * 0.36;
    alphaMask += airWobble * waveBands * (1.0 - radialFade) * 0.45;
    alphaMask *= active;
    float finalAlpha = saturate(alpha * alphaMask);
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 highEnergy = vec3(1.0, 0.94, 0.78);
    vec3 edgeColor = mix(color, highEnergy, saturate(edgeMask * 0.38 + waveBands * 0.10));
    vec3 coreColor = mix(color, vec3(0.86, 0.98, 1.0), saturate(throatGlow * 1.8));
    vec3 finalColor = edgeColor * (0.62 + waveBands * 0.38 + edgeMask * 0.28);
    finalColor += coreColor * (throatGlow * 1.32 + airWobble * 0.36);
    finalColor *= brightness;

    outputColor(vec4(finalColor, finalAlpha));
}
