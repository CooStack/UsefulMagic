#version 330 core

in vec3 vLocalDir;
in vec3 vViewPos;
in float vWave;
in float vFineWave;

uniform vec3 baseColor = vec3(0.42, 0.72, 1.0);
uniform vec3 energyColor = vec3(0.72, 0.94, 1.0);
uniform vec3 rimColor = vec3(0.96, 0.99, 1.0);
uniform float time = 0.0;
uniform float alpha = 1.0;
uniform float deployScale = 1.0;
uniform float collapseProgress = 0.0;
uniform float membraneOpacity = 0.12;
uniform float energyOpacity = 0.86;
uniform float rimOpacity = 1.22;
uniform float filamentDensity = 15.0;
uniform float filamentWidth = 0.032;
uniform float edgeWidth = 0.11;
uniform float cameraInside = 0.0;
uniform int passMode = 0;
uniform int renderTarget = 2;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 MaskColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void outputColor(vec4 value) {
    FragColor = renderTarget != 1 ? value : vec4(0.0);
    MaskColor = renderTarget != 0 ? value : vec4(0.0);
}

float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.53));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float filament(float coord, float width, float feather) {
    float d = abs(fract(coord) - 0.5);
    float core = 1.0 - smoothstep(width, width + feather, d);
    float halo = 1.0 - smoothstep(width * 2.6, width * 2.6 + feather * 2.4, d);
    return max(core, halo * 0.38);
}

void main() {
    vec3 normal = normalize(vLocalDir);
    vec3 viewDir = normalize(-vViewPos);
    vec3 faceNormal = gl_FrontFacing ? normal : -normal;
    float ndv = saturate(abs(dot(faceNormal, viewDir)));
    float rim = pow(1.0 - ndv, 1.38);
    float hardRim = pow(1.0 - ndv, 4.6);
    float backFace = gl_FrontFacing ? 0.0 : 1.0;

    float u = atan(normal.z, normal.x) / TAU + 0.5;
    float v = acos(clamp(normal.y, -1.0, 1.0)) / PI;
    vec2 uv = vec2(u, v);

    float density = max(filamentDensity, 5.0);
    float width = clamp(filamentWidth, 0.006, 0.14);
    float edge = clamp(edgeWidth, 0.025, 0.32);
    float driftA = time * 0.030;
    float driftB = time * 0.052;
    float slowNoise = valueNoise(uv * vec2(10.0, 6.0) + vec2(driftA, -driftB));
    float fineNoise = valueNoise(uv * vec2(34.0, 17.0) + vec2(-driftB, driftA));

    float latBend = sin(u * TAU * 3.0 + time * 0.11 + vWave * 0.65) * 0.045;
    float lonBend = sin(v * TAU * 4.0 - time * 0.14 + vFineWave * 0.35) * 0.038;
    float latitude = filament((v + latBend) * density, width, 0.018);
    float longitude = filament((u + lonBend) * density * 1.22 - time * 0.018, width * 0.78, 0.016);
    float arcA = filament((u + v * 0.58 + slowNoise * 0.030) * density * 0.72 + time * 0.015, edge * 0.30, 0.030);
    float arcB = filament((u - v * 0.64 + fineNoise * 0.024) * density * 0.66 - time * 0.012, edge * 0.24, 0.026);
    float filamentMask = saturate(max(max(latitude, longitude), max(arcA, arcB) * 0.78));

    float shimmer = 0.5 + 0.5 * sin((u * 9.0 + v * 6.0 + vWave * 0.70) * TAU - time * 0.45);
    float energyField = smoothstep(0.32, 0.98, shimmer * 0.55 + slowNoise * 0.34 + fineNoise * 0.18);
    float deployFade = smoothstep(0.02, 0.25, deployScale);
    float collapseFade = pow(1.0 - saturate(collapseProgress), 1.14);
    float insideClear = mix(1.0, 0.34, saturate(cameraInside));
    float outsideEnergy = mix(1.0, 0.62, saturate(cameraInside));

    float membrane = membraneOpacity * (0.18 + slowNoise * 0.14 + energyField * 0.08);
    float threads = filamentMask * energyOpacity * (0.28 + energyField * 0.72);
    float edgeGlow = (rim * rimOpacity + hardRim * 2.6) * (0.64 + filamentMask * 0.44);
    float exteriorDepth = 1.0 + backFace * 0.20;

    float bodyAlpha = membrane + threads * 0.24 + edgeGlow * 0.24;
    float glowAlpha = threads * 0.24 + edgeGlow * 0.52 + hardRim * 0.56;
    float maskAlpha = threads * 0.08 + edgeGlow * 0.42 + hardRim * 0.86;
    float glowMode = passMode == 1 ? 1.0 : 0.0;
    float maskMode = passMode == 2 ? 1.0 : 0.0;
    float finalAlpha = mix(bodyAlpha, glowAlpha, glowMode);
    finalAlpha = mix(finalAlpha, maskAlpha, maskMode);
    finalAlpha *= alpha * deployFade * collapseFade * insideClear * outsideEnergy * exteriorDepth;

    vec3 membraneColor = mix(baseColor * 0.44, baseColor, slowNoise * 0.36 + energyField * 0.34);
    vec3 threadColor = mix(baseColor, energyColor, saturate(0.35 + energyField * 0.50 + filamentMask * 0.20));
    vec3 edgeColor = mix(energyColor, rimColor, saturate(rim + hardRim * 0.45));
    vec3 color = membraneColor * membrane * 0.95;
    color += threadColor * threads * (0.82 + outsideEnergy * 0.24);
    color += edgeColor * edgeGlow * 0.86;
    color += rimColor * hardRim * (0.78 + collapseProgress * 0.42);

    if (passMode == 1) {
        color = threadColor * threads * 0.88 + edgeColor * edgeGlow * 1.10 + rimColor * hardRim * 1.16;
        color *= 0.74 + energyField * 0.24 + collapseProgress * 0.18;
    }

    if (passMode == 2) {
        color = threadColor * threads * 0.16 + edgeColor * edgeGlow * 0.74 + rimColor * hardRim * 1.16;
        color *= 0.72 + energyField * 0.18 + collapseProgress * 0.12;
    }

    color *= deployFade * collapseFade;
    if (finalAlpha <= 0.002) {
        discard;
    }
    outputColor(vec4(color, saturate(finalAlpha)));
}
