#version 330 core

in vec3 vLocalDir;
in vec3 vViewPos;
in float vShellWave;

uniform sampler2D surfaceTexture;
uniform sampler2D cloudTexture;
uniform vec3 color = vec3(1.0, 1.0, 1.0);
uniform float alpha = 1.0;
uniform float brightness = 1.0;
uniform float time = 0.0;
uniform float discardProgress = 0.0;
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

float luma(vec3 value) {
    return dot(value, vec3(0.299, 0.587, 0.114));
}

vec3 safeTint(vec3 value) {
    vec3 clamped = clamp(value, vec3(0.0), vec3(1.0));
    float strongest = max(max(clamped.r, clamped.g), clamped.b);
    return mix(vec3(1.0), clamped, step(0.05, strongest));
}

vec3 rotateY(vec3 dir, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return normalize(vec3(
        dir.x * c - dir.z * s,
        dir.y,
        dir.x * s + dir.z * c
    ));
}

vec2 sphereUv(vec3 dir) {
    float u = atan(dir.z, dir.x) / TAU + 0.5;
    float v = acos(clamp(dir.y, -1.0, 1.0)) / PI;
    return vec2(u, v);
}

float energyField(vec3 dir, float spin) {
    vec2 uvA = sphereUv(rotateY(dir, spin));
    vec2 uvB = sphereUv(rotateY(dir, -spin * 0.72 + 1.35));
    vec3 surfaceA = texture(surfaceTexture, fract(uvA * vec2(2.45, 1.56) + vec2(time * 0.026, -time * 0.017))).rgb;
    vec3 surfaceB = texture(surfaceTexture, fract(uvB * vec2(4.35, 2.74) + vec2(-time * 0.018, time * 0.029))).gbr;
    float broadFlow = luma(surfaceA) * 0.62 + luma(surfaceB) * 0.38;
    float streamLines = smoothstep(0.18, 0.72, abs(luma(surfaceA) - luma(surfaceB)) + vShellWave * 0.10 + 0.18);
    return saturate((broadFlow - 0.08) * 1.24 + streamLines * 0.16 + vShellWave * 0.055);
}

void main() {
    vec3 normal = normalize(vLocalDir);
    vec3 viewDir = normalize(-vViewPos);
    vec3 faceNormal = gl_FrontFacing ? normal : -normal;
    float ndv = saturate(abs(dot(faceNormal, viewDir)));
    float rim = pow(1.0 - ndv, 1.55);
    float hardRim = pow(1.0 - ndv, 4.35);
    float releaseFade = pow(1.0 - saturate(discardProgress), 1.12);
    vec3 tint = safeTint(color);
    vec3 cloudTint = mix(tint, vec3(1.0), 0.18);

    if (passMode == 1) {
        vec3 cloudDirA = rotateY(normal, time * 0.105);
        vec3 cloudDirB = rotateY(normal, -time * 0.071 + 0.90);
        vec2 cloudUvA = sphereUv(cloudDirA) * vec2(2.22, 1.34) + vec2(time * 0.013, time * 0.005);
        vec2 cloudUvB = sphereUv(cloudDirB) * vec2(3.74, 2.28) + vec2(-time * 0.009, time * 0.016);
        vec2 cloudUvC = sphereUv(rotateY(normal, time * 0.033 + 2.10)) * vec2(5.28, 3.14) + vec2(time * 0.020, -time * 0.014);
        float cloudA = texture(cloudTexture, fract(cloudUvA)).r;
        float cloudB = texture(cloudTexture, fract(cloudUvB)).g;
        float cloudC = texture(cloudTexture, fract(cloudUvC)).b;
        float cloud = saturate((cloudA * 0.42 + cloudB * 0.34 + cloudC * 0.24) + vShellWave * 0.03);
        float cloudMask = smoothstep(0.60, 0.82, cloud);
        float cloudDensity = smoothstep(0.70, 0.94, cloud) * cloudMask;
        float cloudPuff = pow(saturate((cloud - 0.58) * 2.20), 2.55);
        float cloudShape = cloudDensity * (0.32 + cloudPuff * 0.84);
        float finalAlpha = alpha * releaseFade * cloudShape;
        if (finalAlpha <= 0.05) {
            discard;
        }

        vec3 shadow = cloudTint * 0.58;
        vec3 lit = mix(cloudTint, vec3(0.98, 0.99, 1.0), saturate(cloudPuff * 0.24 + cloudDensity * 0.06));
        vec3 cloudColor = mix(shadow, lit, saturate(cloudPuff * 0.42 + cloudDensity * 0.10));
        cloudColor *= brightness * (0.82 + cloudPuff * 0.10);
        outputColor(vec4(cloudColor, saturate(finalAlpha)));
        return;
    }

    float surfaceField = energyField(normal, time * 0.135);
    float innerField = energyField(normal, -time * 0.185 + 0.65);
    float flow = saturate(surfaceField * 0.68 + innerField * 0.42);
    float hotPatch = smoothstep(0.52, 0.90, flow);
    float vein = smoothstep(0.14, 0.36, abs(surfaceField - innerField) + vShellWave * 0.045);
    float body = saturate(0.34 + flow * 0.34 + vein * 0.10 + rim * 0.12);
    vec3 softBase = mix(tint * 0.78, vec3(0.94, 0.98, 1.0), 0.42);
    vec3 brightFlow = mix(tint, vec3(1.0), saturate(hotPatch * 0.58 + hardRim * 0.42));
    vec3 whiteHot = vec3(1.0, 0.96, 0.84);
    vec3 surfaceColor = mix(softBase, brightFlow, saturate(flow * 0.76 + rim * 0.14));
    surfaceColor = mix(surfaceColor, whiteHot, saturate(hotPatch * 0.24 + hardRim * 0.18));

    if (passMode == 2) {
        float maskAlpha = alpha * releaseFade * saturate(hotPatch * 0.30 + vein * 0.10 + rim * 0.18 + hardRim * 0.24);
        if (maskAlpha <= 0.002) {
            discard;
        }
        vec3 maskColor = mix(tint, whiteHot, saturate(hotPatch * 0.30 + hardRim * 0.26));
        maskColor *= brightness * (0.72 + flow * 0.22 + hardRim * 0.22);
        outputColor(vec4(maskColor, maskAlpha));
        return;
    }

    float finalAlpha = alpha * releaseFade * body;
    if (finalAlpha <= 0.002) {
        discard;
    }
    surfaceColor *= brightness * (0.76 + flow * 0.20 + hotPatch * 0.14 + rim * 0.12);
    outputColor(vec4(surfaceColor, saturate(finalAlpha)));
}
