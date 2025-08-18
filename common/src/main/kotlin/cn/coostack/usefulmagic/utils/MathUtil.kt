package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object MathUtil {
    fun getHollowSphere(r: Int): List<RelativeLocation> {
        if (r < 0) return emptyList()
        if (r == 0) return listOf(RelativeLocation(0, 0, 0))

        val points = ArrayList<RelativeLocation>()
        val rSquared = r * r

        // 遍历所有可能的x和y坐标
        for (x in -r..r) {
            for (y in -r..r) {
                val xySq = x * x + y * y
                if (xySq > rSquared) continue

                // 计算z坐标的平方值
                val zSq = rSquared - xySq

                // 处理z=0的情况
                if (zSq == 0) {
                    points.add(RelativeLocation(x, y, 0))
                }
                // 处理z≠0的情况
                else {
                    // 检查zSq是否为完全平方数
                    val zVal = sqrt(zSq.toDouble()).toInt()
                    if (zVal * zVal == zSq) {
                        // 添加正负z两个点
                        points.add(RelativeLocation(x, y, zVal))
                        points.add(RelativeLocation(x, y, -zVal))
                    }
                }
            }
        }
        return points
    }
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
     * 获取以r为半径的内接正n边形的内接圆半径
     */
    fun getPolygonInscribedCircle(n: Int, r: Double): Double {
        if (n < 3) {
            return 0.0
        }
        return r * sin(PI / (2 * n))
    }
    fun discreteCylinderGenerator(
        minDiscrete: Double,
        maxDiscrete: Double,
        maxRadius: Double,
        height: Double,
        heightStep: Double,
        radiusStep: Double,
        minCount: Int,
        maxCount: Int,
    ): List<RelativeLocation> {
        val res = ArrayList<RelativeLocation>()
        var currentHeight = 0.0
        val discreteStep = (maxDiscrete - minDiscrete) / (maxRadius / radiusStep)
        val countStep = ((maxCount - minCount) / (maxRadius / radiusStep)).roundToInt().coerceAtLeast(1)
        while (currentHeight <= height) {
            var currentRadius = radiusStep
            var currentDiscrete = discreteStep
            var currentCount = minCount
            while (currentRadius <= maxRadius) {
                res.addAll(
                    Math3DUtil.getDiscreteCircleXZ(currentRadius, currentCount, currentDiscrete)
                        .onEach { it.y += currentHeight }
                )
                currentCount += countStep
                currentDiscrete += discreteStep
                currentRadius += radiusStep
            }
            currentHeight += heightStep
        }

        return res
    }
}
