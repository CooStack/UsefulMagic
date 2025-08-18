#version 330 core
uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;
uniform float u_time;
uniform float u_radius;

layout (location = 0) in vec3 pos;

out vec3 fragPos;
out vec3 normal;
out float edgeFactor; // 新增：边缘因子

void main() {
    // 保持原始位置不变
    float scaled = u_radius;
    if (scaled < 0.) {
        scaled = 0.;
    }
    vec3 scaledPos = pos * scaled;
    //
    //    // 计算XZ平面上的半径
    //    float r_xz = length(vec2(pos.x, pos.z));
    //
    //    // 只缩放XZ平面
    //    if (r_xz > 0.001) {
    //        float scale = u_radius / r_xz;
    //        scaledPos.x = pos.x * scale;
    //        scaledPos.z = pos.z * scale;
    //    }

    // 计算法线 - 圆柱的法线在XZ平面上
    //    vec2 normalXZ = normalize(vec2(pos.x, pos.z));
    //    normal = vec3(normalXZ.x, 0.0, normalXZ.y);
    normal = normalize(pos);
    // 计算边缘因子 (0=中心, 1=边缘)
    edgeFactor = 1.0 - smoothstep(0.0, scaled * 0.8, length(pos));

    // 应用变换矩阵
    vec4 worldPos = transMat * vec4(scaledPos, 1.0);
    fragPos = worldPos.xyz;

    // 计算最终位置
    gl_Position = projMat * viewMat * worldPos;
}