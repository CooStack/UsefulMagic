#version 330 core

layout (location = 0) in vec3 pos;
uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 transMat;

out vec3 point;
out vec3 normal;

void main() {
    // 应用完整的模型变换（包括平移和缩放）
    vec4 worldPos = transMat * vec4(pos, 1.0);
    // 计算法线 - 对于球体，法线就是归一化的顶点位置
    // 注意：这里我们使用原始顶点位置，因为球体的法线不随平移变化
    normal = normalize(pos);
    // 传递世界坐标
    point = worldPos.xyz;
    // 计算裁剪空间位置
    gl_Position = projMat * viewMat * worldPos;
}