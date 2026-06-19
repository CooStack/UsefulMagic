#version 330 core

out vec4 FragColor;

in vec2 vUv;
in vec2 vLocalUv;

uniform sampler2D shotWaveTexture;
uniform float alpha = 1.0;
uniform float time = 0.0;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void main() {
    vec4 texel = texture(shotWaveTexture, vUv);
    float radius = length(vLocalUv);
    float edgeFade = 1.0 - smoothstep(0.88, 1.04, radius);
    float pulse = 0.92 + 0.08 * sin(time * 0.55);
    float finalAlpha = saturate(texel.a * alpha * edgeFade * pulse);
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 color = texel.rgb * mix(1.0, 1.35, saturate(texel.a));
    FragColor = vec4(color, finalAlpha);
}
