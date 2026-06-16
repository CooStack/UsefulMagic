package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object MathUtil {
    private const val EPS = 1e-7
    private val BOX_EDGES = arrayOf(
        intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7),
        intArrayOf(0, 2), intArrayOf(1, 3), intArrayOf(4, 6), intArrayOf(5, 7),
        intArrayOf(0, 1), intArrayOf(2, 3), intArrayOf(4, 5), intArrayOf(6, 7),
    )

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

    private fun intersectsCylinder(
        start: Vec3,
        end: Vec3,
        radius: Double,
        center: Vec3,
        hitBox: HitBox,
    ): Boolean {
        if (radius < 0.0) return false
        if (start.distanceToSqr(end) <= EPS * EPS) {
            return distanceSqrToHitBox(start, center, hitBox) <= radius * radius + EPS
        }
        return intersectsCylinderWithBoxCorners(start, end, radius, hitBoxCorners(center, hitBox))
    }

    /**
     * 判定从 start 沿 direction 偏移、半径为 radius 的圆柱是否和以 center 为中心的 HitBox 相交。
     */
    fun isIntersectsBox(
        start: Vec3,
        direction: Vec3,
        radius: Double,
        center: Vec3,
        hitBox: HitBox,
    ): Boolean {
        return intersectsCylinder(start, start.add(direction), radius, center, hitBox)
    }

    fun isIntersectsBox(
        start: Vec3,
        direction: RelativeLocation,
        radius: Double,
        center: Vec3,
        hitBox: HitBox,
    ): Boolean {
        return isIntersectsBox(start, direction.toVector(), radius, center, hitBox)
    }

    /**
     * 判定 start -> end、半径为 radius 的柱体是否和 AABB 相交。
     */
    fun isIntersectsBox(start: Vec3, end: Vec3, radius: Double, box: AABB): Boolean {
        if (radius < 0.0) return false
        if (start.distanceToSqr(end) <= EPS * EPS) {
            return distanceSqrToAabb(start, box) <= radius * radius + EPS
        }
        return intersectsCylinderWithBoxCorners(start, end, radius, aabbCorners(box))
    }

    private fun intersectsCylinderWithBoxCorners(
        start: Vec3,
        end: Vec3,
        radius: Double,
        corners: List<Vec3>,
    ): Boolean {
        val axis = end.subtract(start)
        val length = axis.length()
        val axisUnit = axis.scale(1.0 / length)
        val base = if (abs(axisUnit.y) < 0.9) Vec3(0.0, 1.0, 0.0) else Vec3(1.0, 0.0, 0.0)
        val sideX = axisUnit.cross(base).normalize()
        val sideY = axisUnit.cross(sideX).normalize()
        val points = ArrayList<Point2>()

        fun addProjected(point: Vec3) {
            val relative = point.subtract(start)
            val projected = Point2(relative.dot(sideX), relative.dot(sideY))
            if (points.none { it.distanceSqr(projected) <= EPS * EPS }) {
                points += projected
            }
        }

        fun axisDistance(point: Vec3): Double {
            return point.subtract(start).dot(axisUnit)
        }

        val distances = DoubleArray(corners.size) { axisDistance(corners[it]) }
        for (i in corners.indices) {
            if (distances[i] >= -EPS && distances[i] <= length + EPS) {
                addProjected(corners[i])
            }
        }

        for (edge in BOX_EDGES) {
            val firstIndex = edge[0]
            val secondIndex = edge[1]
            val firstDistance = distances[firstIndex]
            val secondDistance = distances[secondIndex]
            val delta = secondDistance - firstDistance
            if (abs(delta) <= EPS) continue

            for (plane in doubleArrayOf(0.0, length)) {
                val t = (plane - firstDistance) / delta
                if (t >= -EPS && t <= 1.0 + EPS) {
                    addProjected(lerp(corners[firstIndex], corners[secondIndex], t.coerceIn(0.0, 1.0)))
                }
            }
        }

        if (points.isEmpty()) return false
        return projectedHullIntersectsCircle(points, radius)
    }

    private fun projectedHullIntersectsCircle(points: List<Point2>, radius: Double): Boolean {
        val radiusSqr = radius * radius + EPS
        if (points.any { it.lengthSqr() <= radiusSqr }) return true

        val hull = convexHull(points)
        if (hull.size >= 3 && containsOrigin(hull)) return true
        if (hull.size == 1) return hull[0].lengthSqr() <= radiusSqr

        for (i in hull.indices) {
            val next = hull[(i + 1) % hull.size]
            if (distanceSqrToOriginSegment(hull[i], next) <= radiusSqr) return true
        }
        return false
    }

    private fun hitBoxCorners(center: Vec3, hitBox: HitBox): List<Vec3> {
        val rotation = Quaterniond().rotateY(-hitBox.yaw).rotateX(-hitBox.pitch)
        val vector = Vector3d()
        val corners = ArrayList<Vec3>(8)
        for (x in doubleArrayOf(hitBox.x1, hitBox.x2)) {
            for (y in doubleArrayOf(hitBox.y1, hitBox.y2)) {
                for (z in doubleArrayOf(hitBox.z1, hitBox.z2)) {
                    vector.set(x, y, z).rotate(rotation)
                    corners += Vec3(center.x + vector.x, center.y + vector.y, center.z + vector.z)
                }
            }
        }
        return corners
    }

    private fun aabbCorners(box: AABB): List<Vec3> {
        val corners = ArrayList<Vec3>(8)
        for (x in doubleArrayOf(box.minX, box.maxX)) {
            for (y in doubleArrayOf(box.minY, box.maxY)) {
                for (z in doubleArrayOf(box.minZ, box.maxZ)) {
                    corners += Vec3(x, y, z)
                }
            }
        }
        return corners
    }

    private fun distanceSqrToHitBox(point: Vec3, center: Vec3, hitBox: HitBox): Double {
        val rotation = Quaterniond().rotateY(-hitBox.yaw).rotateX(-hitBox.pitch).conjugate()
        val local = Vector3d(point.x - center.x, point.y - center.y, point.z - center.z).rotate(rotation)
        val closestX = local.x.coerceIn(hitBox.x1, hitBox.x2)
        val closestY = local.y.coerceIn(hitBox.y1, hitBox.y2)
        val closestZ = local.z.coerceIn(hitBox.z1, hitBox.z2)
        val dx = local.x - closestX
        val dy = local.y - closestY
        val dz = local.z - closestZ
        return dx * dx + dy * dy + dz * dz
    }

    private fun distanceSqrToAabb(point: Vec3, box: AABB): Double {
        val closestX = point.x.coerceIn(box.minX, box.maxX)
        val closestY = point.y.coerceIn(box.minY, box.maxY)
        val closestZ = point.z.coerceIn(box.minZ, box.maxZ)
        val dx = point.x - closestX
        val dy = point.y - closestY
        val dz = point.z - closestZ
        return dx * dx + dy * dy + dz * dz
    }

    private fun lerp(start: Vec3, end: Vec3, t: Double): Vec3 {
        return Vec3(
            start.x + (end.x - start.x) * t,
            start.y + (end.y - start.y) * t,
            start.z + (end.z - start.z) * t,
        )
    }

    private fun convexHull(points: List<Point2>): List<Point2> {
        if (points.size <= 2) return points
        val sorted = points.sortedWith(compareBy<Point2> { it.x }.thenBy { it.y })
        val lower = ArrayList<Point2>()
        for (point in sorted) {
            while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), point) <= EPS) {
                lower.removeAt(lower.lastIndex)
            }
            lower += point
        }
        val upper = ArrayList<Point2>()
        for (point in sorted.asReversed()) {
            while (upper.size >= 2 && cross(upper[upper.size - 2], upper.last(), point) <= EPS) {
                upper.removeAt(upper.lastIndex)
            }
            upper += point
        }
        return (lower.dropLast(1) + upper.dropLast(1)).ifEmpty { sorted.take(1) }
    }

    private fun containsOrigin(hull: List<Point2>): Boolean {
        var sign = 0
        for (i in hull.indices) {
            val a = hull[i]
            val b = hull[(i + 1) % hull.size]
            val cross = (b.x - a.x) * -a.y - (b.y - a.y) * -a.x
            if (abs(cross) <= EPS) continue
            val currentSign = if (cross > 0.0) 1 else -1
            if (sign != 0 && sign != currentSign) return false
            sign = currentSign
        }
        return true
    }

    private fun cross(a: Point2, b: Point2, c: Point2): Double {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    private fun distanceSqrToOriginSegment(a: Point2, b: Point2): Double {
        val abX = b.x - a.x
        val abY = b.y - a.y
        val abLengthSqr = abX * abX + abY * abY
        if (abLengthSqr <= EPS * EPS) return a.lengthSqr()
        val t = (-(a.x * abX + a.y * abY) / abLengthSqr).coerceIn(0.0, 1.0)
        val closestX = a.x + abX * t
        val closestY = a.y + abY * t
        return closestX * closestX + closestY * closestY
    }

    private data class Point2(val x: Double, val y: Double) {
        fun lengthSqr(): Double = x * x + y * y

        fun distanceSqr(other: Point2): Double {
            val dx = x - other.x
            val dy = y - other.y
            return dx * dx + dy * dy
        }
    }
}
