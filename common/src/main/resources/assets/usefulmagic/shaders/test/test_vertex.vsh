#version 330 core
uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;
uniform float u_time;
layout (location = 0) in vec3 pos;

out vec3 fragPos; // 传递片段位置到片段着色器
out vec3 normal;  // 传递法线到片段着色器

void main() {
    // 计算原始半径
    float r_original = length(pos);

    // 根据时间调整半径（脉动效果）
    float pulse = abs(sin(u_time)) * 0.99 + 0.01; // 0.1-1.0之间脉动
    float r_scaled = r_original * pulse;

    // 计算位置（保持球体形状）
    vec3 pos_normal = pos * (r_scaled / r_original);

    // 计算法线（球体法线就是归一化的位置向量）
    normal = normalize(pos);

    // 传递位置到片段着色器
    fragPos = vec3(transMat * vec4(pos_normal, 1.0));

    // 计算最终位置
    gl_Position = projMat * viewMat * vec4(fragPos, 1.0);
}