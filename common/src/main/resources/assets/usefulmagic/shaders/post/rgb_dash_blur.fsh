#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D scene;
uniform vec2 center = vec2(0.5);
uniform vec2 screenSize = vec2(1920.0, 1080.0);
uniform float progress = 0.0;
uniform float time = 0.0;
uniform float strength = 1.0;
uniform float chromaticStrength = 1.0;
uniform float blurStrength = 1.0;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
}

vec3 sampleScene(vec2 uv) {
    return texture(scene, safeUv(uv)).rgb;
}

void main() {
    vec4 base = texture(scene, screen_uv);
    float t = saturate(progress);
    float aspect = screenSize.x / max(screenSize.y, 1.0);
    vec2 toCenter = center - screen_uv;
    float dist = length((screen_uv - center) * vec2(aspect, 1.0)) * 1.34;
    float edge = smoothstep(0.16, 0.96, dist);
    float fade = pow(1.0 - t, 0.72);
    float pulse = saturate(fade * max(strength, 0.0));
    float edgePower = edge * pulse;

    vec2 inward = normalize(toCenter + vec2(0.0001));
    float blurSpan = (0.008 + edge * 0.032) * max(blurStrength, 0.0) * edgePower;

    vec3 blur = base.rgb * 0.24;
    float weightSum = 0.24;
    for (int i = 1; i <= 8; i++) {
        float stepValue = float(i) / 8.0;
        float weight = mix(0.15, 0.035, stepValue);
        vec2 sampleUv = screen_uv + inward * blurSpan * stepValue * (1.0 + stepValue * 1.8);
        blur += sampleScene(sampleUv) * weight;
        weightSum += weight;
    }
    blur /= max(weightSum, 0.0001);

    float chromaOffset = (0.002 + edge * 0.014) * max(chromaticStrength, 0.0) * edgePower;
    vec3 chroma;
    chroma.r = texture(scene, safeUv(screen_uv - inward * chromaOffset * 0.90)).r;
    chroma.g = texture(scene, safeUv(screen_uv + inward * chromaOffset * 0.15)).g;
    chroma.b = texture(scene, safeUv(screen_uv + inward * chromaOffset * 1.05)).b;

    vec3 color = mix(base.rgb, blur, saturate(edgePower * 0.82 * max(blurStrength, 0.0)));
    color = mix(color, chroma, saturate(edgePower * 0.92 * max(chromaticStrength, 0.0)));

    float centerLift = (1.0 - smoothstep(0.0, 0.55, dist)) * pulse * 0.08;
    float edgeDarken = edgePower * 0.16;
    color = color * (1.0 - edgeDarken) + vec3(centerLift);

    FragColor = vec4(max(color, vec3(0.0)), base.a);
}
