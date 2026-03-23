#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D tex;

vec3 softLimit(vec3 value, float limit) {
    return value / (1.0 + value / max(limit, 1.0e-3));
}

void main() {
    vec4 beam = texture(tex, screen_uv);
    float presence = clamp(beam.a + max(beam.r, max(beam.g, beam.b)) * 0.78, 0.0, 1.0);
    vec3 boosted = beam.rgb * (2.4 + presence * 2.8);
    boosted += beam.rgb * vec3(0.9, 1.1, 1.8);
    boosted = max(boosted - vec3(0.12), vec3(0.0));
    boosted = softLimit(boosted, 14.0);
    FragColor = vec4(boosted * presence, 1.0);
}
