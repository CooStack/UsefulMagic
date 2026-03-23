#version 330 core

out vec4 FragColor;

in vec3 beamCoord;

uniform vec3 color = vec3(0.28, 0.82, 1.0);
uniform float alpha = 0.24;
uniform float brightness = 1.6;
uniform float phaseProgress = 1.0;
uniform float collapse = 0.0;
uniform float time = 0.0;
uniform sampler2D impactNoise;
uniform int layerMode = 0;

const float TEXTURE_FLOW_SPEED = 3.0;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float pulse(float value) {
    return 0.5 + 0.5 * sin(value);
}

vec2 sampleImpactOffset(vec2 uv, float radial) {
    float textureTime = time * TEXTURE_FLOW_SPEED;
    vec2 fastUv = vec2(uv.x * 1.35 + textureTime * 0.82, uv.y * 0.72 - textureTime * 0.18);
    vec2 slowUv = vec2(uv.x * 0.48 - textureTime * 0.22, uv.y * 1.58 - textureTime * 0.34);
    vec2 broadUv = vec2(uv.x * 0.22 + textureTime * 0.10, uv.y * 0.34 - textureTime * 0.08);
    vec2 fastNoise = texture(impactNoise, fract(fastUv)).rg * 2.0 - 1.0;
    vec2 slowNoise = texture(impactNoise, fract(slowUv)).gr * 2.0 - 1.0;
    float broadNoise = texture(impactNoise, fract(broadUv)).r * 2.0 - 1.0;
    float edgeWeight = smoothstep(0.12, 0.96, radial);
    vec2 rollingOffset = fastNoise * 0.088 + slowNoise * 0.054;
    rollingOffset.x += broadNoise * 0.064 * edgeWeight;
    rollingOffset.y += (fastNoise.x - slowNoise.y) * 0.038;
    return rollingOffset;
}

void main() {
    float axial = saturate(1.0 - abs(beamCoord.y * 2.0 - 1.0));
    float baseRadial = saturate(length(beamCoord.xz));
    float angle = atan(beamCoord.z, beamCoord.x) / 6.2831853 + 0.5;
    vec2 impactUv = vec2(angle * 2.0 + baseRadial * 0.18, beamCoord.y * 3.2);
    vec2 impactOffset = sampleImpactOffset(impactUv, baseRadial);
    float axialShift = impactOffset.y * (0.78 + phaseProgress * 0.34) * (1.0 - collapse * 0.65);
    float radialShift = impactOffset.x * (0.92 + phaseProgress * 0.42) * (1.0 - collapse);
    float radial = saturate(baseRadial + radialShift);
    float disturbedAxial = saturate(axial + axialShift * 0.42);
    float textureTime = time * TEXTURE_FLOW_SPEED;
    float smoke = texture(impactNoise, fract(vec2(angle * 3.4 - textureTime * 0.46, beamCoord.y * 4.7 - textureTime * 0.58))).r;
    float smokeFine = texture(impactNoise, fract(vec2(angle * 6.2 + textureTime * 0.34, beamCoord.y * 8.4 - textureTime * 0.92))).g;
    float smokeField = saturate(smoke * 0.72 + smokeFine * 0.38 + abs(impactOffset.x) * 0.72);
    float smokeGroove = smoothstep(0.58, 0.90, smokeField);
    float smokeRidge = smoothstep(0.32, 0.68, smokeField) * (1.0 - smoothstep(0.76, 0.98, smokeField));
    float core = pow(saturate(1.0 - radial), 3.8);
    float hotCore = pow(saturate(1.0 - radial), 10.0);
    float shell = pow(saturate(1.0 - radial), 1.35);
    float rim = pow(saturate(1.0 - abs(radial - (0.78 + impactOffset.x * 0.28)) / 0.24), 1.6);
    float shock = smoothstep(0.32, 0.88, smoke + abs(impactOffset.x) * 1.15) * smoothstep(0.08, 0.92, baseRadial);
    float flow = pulse(time * 12.0 - (beamCoord.y + axialShift) * 24.0 + radial * 8.0 + smoke * 5.6);
    float wave = smoothstep(0.0, 0.22 + phaseProgress * 0.18, disturbedAxial);
    float body = shell * 0.24 + core * 0.38 + rim * (0.24 + shock * 0.16) + flow * 0.08 + wave * 0.10 + shock * 0.12 + smokeRidge * 0.30;
    body *= 1.0 - smokeGroove * 0.58;

    float collapseFade = pow(1.0 - collapse, 1.35 + beamCoord.y * 0.4);
    vec3 outer = mix(color * 0.76, vec3(0.38, 0.66, 0.92), 0.20);
    vec3 whiteHot = vec3(1.0, 0.98, 0.92);

    if (layerMode == 1) {
        float glowBody = hotCore * 1.08 + core * 0.18 + wave * 0.035;
        float finalAlpha = saturate(alpha * (glowBody + 0.004)) * collapseFade;
        if (finalAlpha <= 0.002) {
            discard;
        }

        vec3 beamColor = mix(color * 0.64, whiteHot, saturate(hotCore * 1.45 + core * 0.10));
        beamColor *= brightness * (0.72 + hotCore * 0.76 + core * 0.18);
        FragColor = vec4(beamColor, finalAlpha);
        return;
    }

    float shellVisibility = mix(0.36, 1.0, smoothstep(0.16, 0.82, baseRadial));
    float outerBody = rim * (0.28 + shock * 0.14) + smokeRidge * 0.46 + shell * 0.14 + flow * 0.055 + wave * 0.06;
    outerBody *= 1.0 - smokeGroove * 0.64;
    float finalAlpha = saturate(alpha * (outerBody + 0.012 + smokeRidge * 0.06)) * collapseFade * shellVisibility;
    finalAlpha *= 1.0 - smokeGroove * 0.50;
    if (finalAlpha <= 0.002) {
        discard;
    }

    if (layerMode == 2) {
        float maskAlpha = saturate(finalAlpha * 1.85 + smokeRidge * alpha * 0.18 + rim * alpha * 0.10);
        vec3 maskColor = mix(color * 0.82, vec3(1.0, 0.76, 0.98), saturate(smokeRidge * 0.36 + rim * 0.20));
        maskColor *= brightness * (0.88 + smokeRidge * 0.34 + rim * 0.22 + shock * 0.16);
        FragColor = vec4(maskColor, maskAlpha);
        return;
    }

    vec3 smokeTint = mix(color * 0.58, vec3(0.70, 0.88, 1.0), smokeRidge * 0.22);
    vec3 beamColor = mix(outer, smokeTint, saturate(smokeRidge * 0.62 + rim * 0.24));
    beamColor = mix(beamColor, color * 0.48, smokeGroove * 0.52);
    beamColor *= brightness * (0.52 + rim * 0.16 + flow * 0.04 + wave * 0.04 + shock * 0.08 + smokeRidge * 0.24);
    beamColor *= 1.0 - smokeGroove * 0.34;

    FragColor = vec4(beamColor, finalAlpha);
}
