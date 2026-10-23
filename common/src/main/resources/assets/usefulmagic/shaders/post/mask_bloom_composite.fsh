#version 330 core

in vec2 screen_uv;
out vec4 FragColor;

uniform sampler2D SceneColor;
uniform sampler2D BloomAtlas;
uniform sampler2D Mask;
uniform int MipLevels = 7;
uniform float Intensity = 3.0;
uniform float BaseMaskIntensity = 0.0;

// 场景已完成色调映射，按内置 BSL 合成规则将 HDR 辉光转换为透射率。
vec3 compositeHdrBloom(vec3 sceneColor, vec3 bloomColor) {
    vec3 scene = clamp(sceneColor, vec3(0.0), vec3(1.0));
    vec3 transmission = exp(-max(bloomColor, vec3(0.0)) * 0.2);
    return vec3(1.0) - (vec3(1.0) - scene) * transmission;
}

vec3 decodeBloom(vec3 encodedBloom) {
    vec3 bloom = max(encodedBloom, vec3(0.0));
    bloom *= bloom;
    return bloom * bloom * 32.0;
}

// 半 texel 偏移对应 atlas 的 gutter，避免相邻尺度串色。
vec4 sampleBloomTile(float lod, vec2 coord, vec2 offset) {
    vec2 atlasSize = max(vec2(textureSize(BloomAtlas, 0)), vec2(1.0));
    float resolutionScale = 1.25 * min(720.0, atlasSize.y) / atlasSize.y;
    vec2 uv = (coord / exp2(lod) + offset) * resolutionScale;
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThanEqual(uv, vec2(1.0)))) {
        return vec4(0.0);
    }
    vec4 encodedBloom = texture(BloomAtlas, uv);
    return vec4(decodeBloom(encodedBloom.rgb), encodedBloom.a);
}

float bayer2(vec2 position) {
    position = floor(position);
    return fract(position.x * 0.5 + position.y * position.y * 0.75);
}

float bayer4(vec2 position) {
    return bayer2(position * 0.5) * 0.25 + bayer2(position);
}

float bayer8(vec2 position) {
    return bayer4(position * 0.5) * 0.25 + bayer2(position);
}

vec3 ditherRgba8(vec3 color, float threshold) {
    vec3 scaled = clamp(color, vec3(0.0), vec3(1.0)) * 255.0;
    return floor(scaled + threshold + 1.0e-4) / 255.0;
}

// 按内置 atlas 的 tile 位置和 BSL 权重重建，不将编码后的 atlas 当作普通颜色纹理。
vec4 reconstructBloom() {
    vec2 atlasSize = max(vec2(textureSize(BloomAtlas, 0)), vec2(1.0));
    vec2 view = 1.0 / atlasSize;
    int levels = clamp(MipLevels, 1, 7);
    vec4 blur1 = sampleBloomTile(1.0, screen_uv, vec2(0.0, 0.0) + vec2(0.5, 0.0) * view);
    if (levels == 1) return blur1;

    vec4 blur2 = sampleBloomTile(2.0, screen_uv, vec2(0.50, 0.0) + vec2(4.5, 0.0) * view);
    if (levels == 2) return (blur1 * 1.23 + blur2) / 2.23;

    vec4 blur3 = sampleBloomTile(3.0, screen_uv, vec2(0.50, 0.25) + vec2(4.5, 4.0) * view);
    if (levels == 3) return (blur1 * 1.71 + blur2 * 1.52 + blur3) / 4.23;

    vec4 blur4 = sampleBloomTile(4.0, screen_uv, vec2(0.625, 0.25) + vec2(8.5, 4.0) * view);
    if (levels == 4) return (blur1 * 2.46 + blur2 * 2.25 + blur3 * 1.71 + blur4) / 7.42;

    vec4 blur5 = sampleBloomTile(5.0, screen_uv, vec2(0.6875, 0.25) + vec2(12.5, 4.0) * view);
    if (levels == 5) return (blur1 * 3.58 + blur2 * 3.35 + blur3 * 2.72 + blur4 * 1.87 + blur5) / 12.52;

    vec4 blur6 = sampleBloomTile(6.0, screen_uv, vec2(0.625, 0.3125) + vec2(8.5, 8.0) * view);
    if (levels == 6) return (blur1 * 5.25 + blur2 * 4.97 + blur3 * 4.20 + blur4 * 3.13 + blur5 * 2.00 + blur6) / 20.55;

    vec4 blur7 = sampleBloomTile(7.0, screen_uv, vec2(0.640625, 0.3125) + vec2(12.5, 8.0) * view);
    return (blur1 * 7.76 + blur2 * 7.41 + blur3 * 6.43 + blur4 * 5.04 + blur5 * 3.51 + blur6 * 2.11 + blur7) / 33.26;
}

void main() {
    vec4 scene = texture(SceneColor, screen_uv);
    vec3 bloom = reconstructBloom().rgb;
    vec3 mask = texture(Mask, screen_uv).rgb;
    // 模糊辉光已在亮部提取阶段乘过 Intensity，这里只为未模糊 mask 应用相同强度。
    vec3 glow = bloom + mask * max(BaseMaskIntensity, 0.0) * max(Intensity, 0.0);
    vec3 result = compositeHdrBloom(scene.rgb, glow);
    FragColor = vec4(ditherRgba8(result, bayer8(gl_FragCoord.xy)), scene.a);
}
