#version 330 core

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 MaskColor;

in vec3 localPos;

uniform vec3 color = vec3(0.22, 0.40, 1.0);
uniform float alpha = 0.24;
uniform float brightness = 1.6;
uniform float rimPower = 1.2;
uniform float coreBias = 0.3;
uniform float highlightStrength = 0.7;
uniform float impactStrength = 0.8;
uniform float textureScale = 1.0;
uniform float textureSpeed = 1.0;
uniform float phaseProgress = 1.0;
uniform float collapse = 0.0;
uniform float time = 0.0;
uniform sampler2D impactNoise;
uniform int renderTarget = 2;

const float TEXTURE_FLOW_SPEED = 3.0;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void outputColor(vec4 value) {
    FragColor = renderTarget != 1 ? value : vec4(0.0);
    MaskColor = renderTarget != 0 ? value : vec4(0.0);
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
    float height01 = saturate(localPos.y);
    float descend = 1.0 - height01;
    float radial = saturate(length(localPos.xz));
    vec3 faceNormal = normalize(cross(dFdx(localPos), dFdy(localPos)));
    if (!gl_FrontFacing) {
        faceNormal = -faceNormal;
    }
    float capMask = step(0.55, abs(faceNormal.y));
    float sideMask = 1.0 - capMask;
    float angle = atan(localPos.z, localPos.x);
    float wrap = angle / 6.28318530718 + 0.5;

    vec2 impactUv = vec2(wrap * 2.0 + radial * 0.18, height01 * 3.2);
    vec2 impactOffset = sampleImpactOffset(impactUv, radial);
    float texturedDescend = saturate(descend + impactOffset.y * 0.42);
    float texturedWrap = wrap + impactOffset.x * 0.16;
    float textureTime = time * TEXTURE_FLOW_SPEED;
    float smoke = texture(impactNoise, fract(vec2(wrap * 3.4 - textureTime * 0.46, height01 * 4.7 - textureTime * 0.58))).r;
    float smokeFine = texture(impactNoise, fract(vec2(wrap * 6.2 + textureTime * 0.34, height01 * 8.4 - textureTime * 0.92))).g;
    float smokeField = saturate(smoke * 0.72 + smokeFine * 0.38 + abs(impactOffset.x) * 0.72);
    float smokeGroove = smoothstep(0.58, 0.90, smokeField);
    float smokeRidge = smoothstep(0.32, 0.68, smokeField) * (1.0 - smoothstep(0.76, 0.98, smokeField));

    float downA = pulse(texturedDescend * textureScale * 24.0 - time * textureSpeed * 7.8 + texturedWrap * 22.0);
    float downB = pulse(texturedDescend * textureScale * 16.0 - time * textureSpeed * 5.2 - texturedWrap * 13.0);
    float downC = pulse(texturedDescend * textureScale * 10.0 - time * textureSpeed * 3.6 + texturedWrap * 9.0);
    float streaks = smoothstep(0.72, 1.0, downA);
    streaks += smoothstep(0.80, 1.0, downB) * 0.82;
    streaks += smoothstep(0.86, 1.0, downC) * 0.56;
    streaks += smokeRidge * 0.94;
    streaks *= 1.0 - smokeGroove * 0.52;

    float waveFront = smoothstep(0.0, 0.18 + phaseProgress * 0.22, descend);
    waveFront *= 1.0 - smoothstep(0.26 + phaseProgress * 0.12, 0.62, descend);
    float capHighlight = pow(1.0 - radial, 1.6);
    float capRing = 1.0 - smoothstep(0.0, 0.12, abs(radial - (0.82 - collapse * 0.52)));
    float sideRim = pow(saturate(streaks * 0.48 + waveFront * 0.28), max(rimPower, 0.1));
    float capRim = pow(radial, max(rimPower, 0.1));

    float sideEnergy = sideMask * (0.20 + streaks * 0.52 + waveFront * highlightStrength * 0.26 + sideRim * 0.28);
    float capEnergy = capMask * (
        0.12 +
        capHighlight * highlightStrength * 0.72 +
        capRim * 0.20 +
        capRing * impactStrength * 0.18
    );
    float energy = sideEnergy + capEnergy;

    float collapseFade = pow(1.0 - collapse, 1.35 + descend * 0.55);
    float finalAlpha = saturate(alpha * (0.12 + energy));
    finalAlpha *= collapseFade;
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 deepBlue = mix(vec3(0.10, 0.28, 1.0), color, 0.78);
    vec3 violet = vec3(0.36, 0.18, 0.94);
    vec3 whiteHot = vec3(0.92, 0.97, 1.0);
    float whiteLift = saturate(coreBias * 0.42 + capHighlight * 0.22 + waveFront * 0.20 + capRing * impactStrength * 0.08);
    float edgeLift = saturate(sideRim * sideMask * 0.36 + capRim * 0.16 + capRing * impactStrength * 0.06);

    vec3 beamColor = mix(deepBlue, violet, 0.12 + edgeLift * 0.26);
    beamColor = mix(beamColor, whiteHot, whiteLift);
    beamColor *= brightness * (1.0 + streaks * sideMask * 0.28 + capHighlight * 0.18 + capRing * impactStrength * 0.08);

    outputColor(vec4(beamColor, finalAlpha));
}
