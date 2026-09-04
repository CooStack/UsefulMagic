#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D SceneColor;
uniform sampler2D Bloom;
uniform sampler2D Mask;
uniform float Intensity = 3.0;
uniform float BaseMaskIntensity = 0.0;

void main() {
    vec4 scene = texture(SceneColor, screen_uv);
    vec3 bloom = texture(Bloom, screen_uv).rgb;
    vec3 mask = texture(Mask, screen_uv).rgb;
    vec3 glow = (bloom + mask * max(BaseMaskIntensity, 0.0)) * max(Intensity, 0.0);
    FragColor = vec4(scene.rgb + glow, scene.a);
}
