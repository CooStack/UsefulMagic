#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D beamMask;
uniform sampler2D brightPrefilter;
uniform sampler2D mediumBloom;
uniform sampler2D wideBloom;

vec3 softLimit(vec3 value, float limit) {
    return value / (1.0 + value / max(limit, 1.0e-3));
}

void main() {
    vec4 beam = texture(beamMask, screen_uv);
    vec3 prefiltered = texture(brightPrefilter, screen_uv).rgb;
    vec3 medium = texture(mediumBloom, screen_uv).rgb;
    vec3 wide = texture(wideBloom, screen_uv).rgb;
    vec3 mip1 = textureLod(brightPrefilter, screen_uv, 1.0).rgb;
    vec3 mip2 = textureLod(brightPrefilter, screen_uv, 2.0).rgb;
    vec3 mip3 = textureLod(brightPrefilter, screen_uv, 3.0).rgb;

    float beamPresence = clamp(beam.a * 1.3 + max(beam.r, max(beam.g, beam.b)) * 0.58, 0.0, 1.0);
    vec3 downsampled = prefiltered * 0.22 + mip1 * 0.42 + mip2 * 0.62 + mip3 * 0.82;
    vec3 upsampled = mip3 * 0.36 + mip2 * 0.52 + mip1 * 0.74;

    float blueBias = clamp(
        dot(prefiltered + medium + wide + beam.rgb, vec3(0.08, 0.22, 0.70)) * 1.26 +
        beam.b * 0.34,
        0.0,
        1.0
    );
    vec3 coreTint = mix(vec3(0.18, 0.34, 1.0), vec3(0.70, 0.84, 1.0), blueBias * 0.42);
    vec3 haloTint = mix(vec3(0.20, 0.26, 0.98), vec3(0.78, 0.88, 1.0), blueBias * 0.38);

    vec3 core = beam.rgb * coreTint * (1.12 + beamPresence * 1.84);
    vec3 glow = core;
    glow += prefiltered * coreTint * 0.44;
    glow += medium * haloTint * 1.42;
    glow += wide * haloTint * 1.78;
    glow += downsampled * haloTint * 0.92;
    glow += upsampled * haloTint * 0.78;
    glow = softLimit(glow, 10.6);

    FragColor = vec4(glow, 1.0);
}
