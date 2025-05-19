package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object MathUtil {

    fun getSolidBall(r: Int): List<RelativeLocation> {
        val rSq = r * r
        val points = ArrayList<RelativeLocation>((4 * r * r * r) / 3) // 预分配容量
        // 仅遍历第一象限 (x ≥ 0, y ≥ 0, z ≥ 0)
        for (x in 0..r) {
            val xSq = x * x
            if (xSq > rSq) break
            val yMax = sqrt((rSq - xSq).toDouble()).toInt()
            for (y in 0..yMax) {
                val ySq = y * y
                val zMax = sqrt((rSq - xSq - ySq).toDouble()).toInt()
                for (z in 0..zMax) {
                    if (xSq + y * y + z * z > rSq) continue

                    // 生成所有符号组合 (2^3=8种可能)
                    val hasX = x > 0
                    val hasY = y > 0
                    val hasZ = z > 0

                    when {
                        // 全非零坐标 → 8个镜像点
                        hasX && hasY && hasZ -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(-x, y, z)
                            points += RelativeLocation(x, -y, z)
                            points += RelativeLocation(x, y, -z)
                            points += RelativeLocation(-x, -y, z)
                            points += RelativeLocation(-x, y, -z)
                            points += RelativeLocation(x, -y, -z)
                            points += RelativeLocation(-x, -y, -z)
                        }
                        // 两个非零坐标 → 4个镜像点
                        hasX && hasY -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(-x, y, z)
                            points += RelativeLocation(x, -y, z)
                            points += RelativeLocation(-x, -y, z)
                        }

                        hasX && hasZ -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(-x, y, z)
                            points += RelativeLocation(x, y, -z)
                            points += RelativeLocation(-x, y, -z)
                        }

                        hasY && hasZ -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(x, -y, z)
                            points += RelativeLocation(x, y, -z)
                            points += RelativeLocation(x, -y, -z)
                        }
                        // 单个非零坐标 → 2个镜像点
                        hasX -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(-x, y, z)
                        }

                        hasY -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(x, -y, z)
                        }

                        hasZ -> {
                            points += RelativeLocation(x, y, z)
                            points += RelativeLocation(x, y, -z)
                        }
                        // 原点 → 1个点
                        else -> {
                            points += RelativeLocation(x, y, z)
                        }
                    }
                }
            }
        }
        return points
    }

    /**
     *
     *
     * 曲线绘制工具
     * 绘制原理: 从原点发射一个点 模拟圆周运动
     * 当点到达 (穿过目标点或者达到目标点时) 停止运行
     * @param direction 圆周运动点的初始方向
     * @param target 从原点发射的点的最终目标位置
     * @param preLineCount 每一个子直线的点的个数
     * @param gravity 目标点的重力 (越大曲线越接近直线) 模拟向心力
     *
     * 需求: 输入的direction的长度不能符合圆周运动的条件 (否则会永无止境的运行)
     * 下列参数输入无法正常运行
     * 1. direction与target方向相反且 direction.length() >= gravity
     * 2. direction.length().pow(2) / target.length() >= gravity
     */
    fun genCurveFromCircularMotion(
        direction: RelativeLocation,
        target: RelativeLocation,
        gravity: Double,
        preLineCount: Int
    ): List<RelativeLocation> {
        require(preLineCount > 0)
        require(gravity > 0)
        val escapeVelocitySq = 2 * gravity * target.length()
        require(direction.length().pow(2) < escapeVelocitySq)
        require((direction.normalize() + target.normalize()).length() !in -1e-3..1e-3 || direction.length() < gravity)
        val res = mutableListOf<RelativeLocation>()
        var current = RelativeLocation()
        var velocity = direction.clone()
        val targetLength = target.length()
        val epsilon = 1e-6
        var iterations = 0
        val maxIterations = 10000

        fun calculateGravity(distance: Double): Double {
            return max(gravity, gravity * (targetLength.pow(2)) / (distance.pow(2) + 1e-6))
        }
        while (iterations++ < maxIterations) {
            val toTarget = target - current
            val distance = toTarget.length()

            if (distance < epsilon) {
                res.add(target)
                break
            }

            val crossProduct = velocity.cross(toTarget)
            if (crossProduct.length() < epsilon && velocity.dot(toTarget) > 0) {
                res.addAll(Math3DUtil.getLineLocations(current, target, preLineCount))
                break
            }

            val prevPosition = current.clone()

            val effectiveGravity = calculateGravity(distance)
            val acceleration = toTarget.normalize().multiply(effectiveGravity)

            velocity = acceleration + direction
            current += velocity

            if (prevPosition.distance(target) < current.distance(target)) {
                res.addAll(Math3DUtil.getLineLocations(prevPosition, target, preLineCount))
                break
            } else {
                res.addAll(Math3DUtil.getLineLocations(prevPosition, current, preLineCount))
            }

            if (iterations > 100 && res.size > 10) {
                val last5 = res.takeLast(5)
                if (last5.all { it.distance(target) < epsilon }) {
                    break
                }
            }
        }

        return res.distinctBy {
            Triple(round(it.x * 1e6), round(it.y * 1e6), round(it.z * 1e6))
        }
    }

    /**
     * 判断point是否在 origin和target形成的直线上
     */
    fun atSameLine(
        origin: RelativeLocation,
        target: RelativeLocation,
        point: RelativeLocation,
    ): Boolean {
        val op = point.clone().remove(origin)
        val tp = point.clone().remove(target)
        return op.cross(tp).length() in -1e-3..1e-3
    }

    fun atSameLine(
        vec1: RelativeLocation,
        vec2: RelativeLocation,
        epsilon: Double = 1e-9
    ): Boolean {
        // 处理零向量特殊情形
        if (vec1.length() < epsilon) return true  // 零向量与任何向量共线
        if (vec2.length() < epsilon) return true

        // 计算叉乘的模长平方避免开根号误差
        val crossX = vec1.y * vec2.z - vec1.z * vec2.y
        val crossY = vec1.z * vec2.x - vec1.x * vec2.z
        val crossZ = vec1.x * vec2.y - vec1.y * vec2.x

        // 优化计算：仅需判断叉乘模长是否趋近于零
        val crossMagnitudeSq = crossX * crossX + crossY * crossY + crossZ * crossZ
        return crossMagnitudeSq < epsilon * epsilon
    }

    /**
     * 获取以r为半径的内接正n边形的内接圆半径
     */
    fun getPolygonInscribedCircle(n: Int, r: Double): Double {
        if (n < 3) {
            return 0.0
        }
        return r * sin(PI / (2 * n))
    }

    /**
     * 通过正n边形的内接圆半径获取到正n边形的外接圆半径
     */
    fun getPolygonCircleRadius(n: Int, r: Double): Double {
        if (n < 3) {
            return 0.0
        }
        return r / sin(PI / (2 * n))
    }

    /**
     * 相同方向且平行
     */
    fun atSameDirection(
        vec1: RelativeLocation,
        vec2: RelativeLocation,
    ): Boolean {
        val x1 = vec1.x / vec2.x
        val y1 = vec1.y / vec2.y
        val z1 = vec1.z / vec2.z
        return x1 == y1 && x1 == z1 && y1 == z1 && x1 == y1 && x1 > 0
    }
}
