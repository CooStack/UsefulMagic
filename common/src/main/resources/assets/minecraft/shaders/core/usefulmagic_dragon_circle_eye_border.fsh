#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float textureAlpha = texture(Sampler0, texCoord0).a;
    if (textureAlpha < 0.01) {
        discard;
    }
    fragColor = vertexColor * ColorModulator * textureAlpha;
}
