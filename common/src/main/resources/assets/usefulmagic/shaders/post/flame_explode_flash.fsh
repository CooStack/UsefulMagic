#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D scene;
uniform vec2 center = vec2(0.5);
uniform vec2 screenSize = vec2(1920.0, 1080.0);
uniform float progress = 0.0;
uniform float time = 0.0;
uniform float strength = 1.0;
uniform float warmth = 1.0;
uniform vec3 flashColor = vec3(0.8, 0.5, 0.2);

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

vec3 saturate(vec3 value) {
    return clamp(value, vec3(0.0), vec3(1.0));
}

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453123);
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
}

void main() {
    vec4 base = texture(scene, screen_uv);
    float t = saturate(progress);
    float aspect = screenSize.x / max(screenSize.y, 1.0);
    vec2 fromCenter = screen_uv - center;
    float dist = length(fromCenter * vec2(aspect, 1.0)) * 1.25;
    float core = 1.0 - smoothstep(0.0, 0.92, dist);

    float instantFlash = exp(-t * 9.0);
    float slowRecovery = pow(1.0 - t, 2.65) * 0.58;
    float flash = max(instantFlash, slowRecovery) * max(strength, 0.0);
    float glare = flash * (0.70 + core * 0.85);

    vec2 radialDir = normalize(fromCenter + vec2(0.0001));
    vec3 smear = texture(scene, safeUv(screen_uv - radialDir * 0.006 * glare)).rgb;
    vec3 tint = mix(flashColor, vec3(1.0, 0.72, 0.32), saturate(warmth));
    vec3 sceneColor = mix(base.rgb, smear, saturate(glare * 0.22));

    sceneColor += tint * glare * (1.15 + core * 1.45);
    sceneColor = mix(sceneColor, tint, saturate(glare * 0.48));
    sceneColor += tint * core * flash * 0.30;

    vec2 grainCell = floor((screen_uv + time * vec2(0.013, 0.021)) * screenSize * 0.55);
    float grain = hash(grainCell) - 0.5;
    sceneColor += vec3(grain * flash * 0.05);

    float exposure = 1.0 + flash * 1.15;
    vec3 exposed = vec3(1.0) - exp(-max(sceneColor, vec3(0.0)) * exposure);
    sceneColor = mix(sceneColor, exposed, saturate(flash * 0.82));
    sceneColor = mix(sceneColor, tint, saturate(core * flash * 0.22));

    FragColor = vec4(saturate(sceneColor), base.a);
}
