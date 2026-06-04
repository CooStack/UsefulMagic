#version 330 core

in vec3 vLocalDir;
in vec3 vViewPos;
in vec3 vWorldPos;
in float vDisturbance;

uniform sampler2D fireTexture;
uniform vec3 color = vec3(1.0, 0.42, 0.10);
uniform float alpha = 1.0;
uniform float brightness = 1.0;
uniform float frontPower = 1.2;
uniform float noiseScale = 1.0;
uniform float flowSpeed = 1.0;
uniform float time = 0.0;
uniform float pulse = 0.5;
uniform int passMode = 0;

out vec4 FragColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float flameSample(vec2 uv, float speed, float channel) {
    vec2 sampleUv = fract(uv + vec2(time * speed * flowSpeed, -time * speed * 0.63 * flowSpeed));
    vec3 tex = texture(fireTexture, sampleUv).rgb;
    return channel < 0.5 ? tex.r : (channel < 1.5 ? tex.g : tex.b);
}

void main() {
    vec3 normal = normalize(vLocalDir);
    vec3 viewDir = normalize(-vViewPos);
    vec3 faceNormal = gl_FrontFacing ? normal : -normal;
    float ndv = saturate(abs(dot(faceNormal, viewDir)));
    float rim = pow(1.0 - ndv, 1.36);

    float front = saturate(normal.y * 0.5 + 0.5);
    float frontMask = pow(front, max(frontPower, 0.1));
    float rearFade = smoothstep(-0.18, 0.34, normal.y);
    float u = atan(normal.z, normal.x) / TAU + 0.5;
    float v = acos(clamp(normal.y, -1.0, 1.0)) / PI;
    vec2 uv = vec2(u, v);

    float flow = time * flowSpeed;
    float coarse = flameSample(uv * vec2(2.0, 1.10) * noiseScale + vec2(flow * 0.034, -flow * 0.052), 0.010, 0.0);
    float tongues = flameSample(vec2(u * 5.8 + vDisturbance * 0.04, v * 2.9 - front * 0.42) * noiseScale, 0.022, 1.0);
    float sharp = flameSample(vec2(u * 12.5 - flow * 0.020, v * 6.4 + front * 0.20) * noiseScale, 0.035, 2.0);

    float turbulent = coarse * 0.48 + tongues * 0.36 + sharp * 0.16;
    float lick = smoothstep(0.38, 0.92, turbulent + front * 0.22 + rim * 0.14);
    float shockRing = 1.0 - smoothstep(0.0, 0.13, abs(front - (0.72 + pulse * 0.08)));
    float noseHot = pow(front, 3.2) * (0.74 + turbulent * 0.46);
    float edgeFire = rim * rearFade * (0.36 + lick * 0.74);

    float surface = frontMask * (0.16 + lick * 0.64 + noseHot * 0.38);
    float energy = surface + edgeFire + shockRing * 0.18;
    if (passMode == 1) {
        energy = frontMask * (noseHot * 0.92 + lick * 0.42 + shockRing * 0.12);
    } else if (passMode == 2) {
        energy = frontMask * (0.22 + lick * 0.82 + noseHot * 0.54 + shockRing * 0.30) + edgeFire * 0.78;
    }

    float finalAlpha = saturate(alpha * energy);
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 ember = vec3(0.42, 0.045, 0.012);
    vec3 orange = color;
    vec3 yellow = vec3(1.0, 0.76, 0.24);
    vec3 whiteHot = vec3(1.0, 0.96, 0.76);
    vec3 flameColor = mix(ember, orange, saturate(turbulent * 1.18 + front * 0.22));
    flameColor = mix(flameColor, yellow, saturate(noseHot * 0.54 + lick * 0.22));
    flameColor = mix(flameColor, whiteHot, saturate(noseHot * 0.28 + shockRing * 0.18));
    flameColor *= brightness * (0.70 + energy * 0.82);

    FragColor = vec4(flameColor, finalAlpha);
}
