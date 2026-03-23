#version 330 core

out vec4 FragColor;

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

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
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

    float downA = pulse(descend * textureScale * 24.0 - time * textureSpeed * 7.8 + wrap * 22.0);
    float downB = pulse(descend * textureScale * 16.0 - time * textureSpeed * 5.2 - wrap * 13.0);
    float downC = pulse(descend * textureScale * 10.0 - time * textureSpeed * 3.6 + wrap * 9.0);
    float streaks = smoothstep(0.72, 1.0, downA);
    streaks += smoothstep(0.80, 1.0, downB) * 0.82;
    streaks += smoothstep(0.86, 1.0, downC) * 0.56;

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
    float edgeLift = saturate(sideRim * 0.36 + capRim * 0.16 + capRing * impactStrength * 0.06);

    vec3 beamColor = mix(deepBlue, violet, 0.12 + edgeLift * 0.26);
    beamColor = mix(beamColor, whiteHot, whiteLift);
    beamColor *= brightness * (1.0 + streaks * 0.28 + capHighlight * 0.18 + capRing * impactStrength * 0.08);

    FragColor = vec4(beamColor, finalAlpha);
}
