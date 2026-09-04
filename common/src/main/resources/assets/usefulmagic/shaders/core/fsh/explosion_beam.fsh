#version 330 core

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 MaskColor;

in vec3 localPos;

uniform vec3 color = vec3(1.0, 0.20, 0.06);
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
uniform float maskIntensityScale = 1.0;
uniform int renderTarget = 2;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void outputColor(vec4 value) {
    FragColor = renderTarget != 1 ? value : vec4(0.0);
    MaskColor = renderTarget != 0 ? vec4(value.rgb * max(maskIntensityScale, 0.0), value.a) : vec4(0.0);
}

float pulse(float value) {
    return 0.5 + 0.5 * sin(value);
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

    float downA = pulse(descend * textureScale * 24.0 - time * textureSpeed * 8.0 + wrap * 18.0);
    float downB = pulse(descend * textureScale * 15.0 - time * textureSpeed * 5.4 - wrap * 12.0);
    float downC = pulse(descend * textureScale * 9.0 - time * textureSpeed * 3.4 + wrap * 8.0);
    float streaks = smoothstep(0.74, 1.0, downA);
    streaks += smoothstep(0.82, 1.0, downB) * 0.78;
    streaks += smoothstep(0.88, 1.0, downC) * 0.52;

    float waveFront = smoothstep(0.0, 0.20 + phaseProgress * 0.24, descend);
    waveFront *= 1.0 - smoothstep(0.30 + phaseProgress * 0.10, 0.66, descend);
    float capHighlight = pow(1.0 - radial, 1.5);
    float capRing = 1.0 - smoothstep(0.0, 0.12, abs(radial - (0.80 - collapse * 0.56)));
    float sideRim = pow(saturate(streaks * 0.54 + waveFront * 0.30), max(rimPower, 0.1));
    float capRim = pow(radial, max(rimPower, 0.1));

    float sideEnergy = sideMask * (0.22 + streaks * 0.56 + waveFront * highlightStrength * 0.28 + sideRim * 0.30);
    float capEnergy = capMask * (
        0.12 +
        capHighlight * highlightStrength * 0.76 +
        capRim * 0.18 +
        capRing * impactStrength * 0.22
    );
    float energy = sideEnergy + capEnergy;

    float collapseFade = pow(1.0 - collapse, 1.4 + descend * 0.55);
    float finalAlpha = saturate(alpha * (0.12 + energy));
    finalAlpha *= collapseFade;
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 ember = mix(vec3(0.34, 0.02, 0.0), color, 0.78);
    vec3 flare = vec3(1.0, 0.34, 0.08);
    vec3 whiteHot = vec3(1.0, 0.95, 0.84);
    float whiteLift = saturate(coreBias * 0.44 + capHighlight * 0.24 + waveFront * 0.24 + capRing * impactStrength * 0.10);
    float edgeLift = saturate(sideRim * 0.40 + capRim * 0.14 + capRing * impactStrength * 0.08);

    vec3 beamColor = mix(ember, flare, 0.18 + edgeLift * 0.34);
    beamColor = mix(beamColor, whiteHot, whiteLift);
    beamColor *= brightness * (1.0 + streaks * 0.30 + capHighlight * 0.22 + capRing * impactStrength * 0.12);

    outputColor(vec4(beamColor, finalAlpha));
}
