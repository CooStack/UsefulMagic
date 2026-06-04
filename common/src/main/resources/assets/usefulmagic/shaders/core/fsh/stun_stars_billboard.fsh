#version 330 core

out vec4 FragColor;

in vec2 vUv;
in vec2 vLocal;

uniform sampler2D spriteTexture;
uniform vec3 color = vec3(1.0, 0.92, 0.44);
uniform float alpha = 1.0;
uniform float brightness = 1.0;
uniform float mode = 0.0;
uniform float time = 0.0;
uniform float trailFade = 1.0;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void main() {
    vec4 tex = texture(spriteTexture, vUv);
    float mask = tex.a;
    vec3 sprite = tex.rgb;

    if (mode > 0.5) {
        float lengthFade = smoothstep(0.0, 0.12, vUv.x) * (1.0 - smoothstep(0.88, 1.0, vUv.x));
        float sideFade = 1.0 - smoothstep(0.18, 0.52, abs(vLocal.y));
        float ripple = 0.86 + 0.14 * sin(vUv.x * 16.0 - time * 0.72);
        mask *= lengthFade * sideFade * ripple * trailFade;
        sprite = mix(vec3(1.0), sprite, 0.45);
    }

    float finalAlpha = saturate(mask * alpha);
    if (finalAlpha <= 0.002) {
        discard;
    }

    vec3 finalColor = mix(color, color * sprite, 0.58) * brightness;
    FragColor = vec4(finalColor, finalAlpha);
}
