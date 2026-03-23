package cn.coostack.usefulmagic.entity.util

import cn.coostack.cooparticlesapi.network.particle.data.DoubleRangeData
import cn.coostack.cooparticlesapi.network.particle.data.isIn
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

object FlightMovementUtil {
    fun brake(
        currentVelocity: Vec3,
        damping: Double = 0.72,
        stopThresholdSqr: Double = 1.0E-4
    ): Vec3 {
        val slowed = currentVelocity.scale(damping)
        return if (slowed.lengthSqr() <= stopThresholdSqr) Vec3.ZERO else slowed
    }

    private fun Vec3.limitLength(maxLength: Double): Vec3 {
        val safeMax = maxLength.coerceAtLeast(0.0)
        if (safeMax <= 0.0) return Vec3.ZERO
        val maxLengthSqr = safeMax * safeMax
        val currentLengthSqr = lengthSqr()
        if (currentLengthSqr <= maxLengthSqr || currentLengthSqr <= 1.0E-8) return this
        return normalize().scale(safeMax)
    }

    /**
     * 1. 与target保持距离 —— 保持相对于目标的偏移位置，偏移向量可随 tick 变化
     *
     * @param currentPosition 实体当前绝对坐标
     * @param currentVelocity 实体当前速度向量
     * @param target 要跟随的目标点（绝对世界坐标）
     * @param acceleration 加速度大小
     * @param damping 每 tick 速度衰减系数，默认 0.82
     * @param idleDistance 空闲距离，在此距离内刹车，默认 0.75
     * @param catchUpDistance 追赶距离，超过此距离加速度 ×1.4，默认 8.0
     */
    fun maintainOffset(
        currentPosition: Vec3,
        currentVelocity: Vec3,
        target: Vec3,
        acceleration: Double,
        damping: Double = 0.82,
        idleDistance: Double = 0.75,
        catchUpDistance: Double = 8.0
    ): Vec3 {
        val delta = target.subtract(currentPosition)
        val dist = delta.length()
        if (dist <= idleDistance) return brake(currentVelocity, damping)
        val dir = delta.scale(1.0 / dist)
        val actualAccel = if (dist > catchUpDistance) acceleration * 1.4 else acceleration
        return currentVelocity.scale(damping).add(dir.scale(actualAccel))
    }

    /**
     * 保持一定的距离， 包含Y轴相对限定范围
     *
     * @param currentPosition
     * @param currentVelocity
     * @param target
     * @param acceleration
     * @param damping
     * @param catchRange 距离小于最小值远离， 大于最大值靠近
     * @param heightRelativeRange 高度差会保持在 heightRelativeRange 设置为null则代表不设置高度差
     * @return
     */
    fun maintainOffset(
        currentPosition: Vec3,
        currentVelocity: Vec3,
        target: Vec3,
        acceleration: Double,
        damping: Double,
        catchRange: DoubleRangeData,
        heightRelativeRange: DoubleRangeData? = null
    ): Vec3 {
        val delta = target - currentPosition
        val distance = delta.length()
        if (distance isIn catchRange) {
            return brake(currentVelocity, damping)
        }

        // 先判断两个基本值
        val actualAccel =
            if (distance > catchRange.max || distance < catchRange.min) acceleration * 1.4 else acceleration
        val sign = if (distance > catchRange.max) 1 else if (distance < catchRange.min) -1 else 0
        var next = currentVelocity * damping + sign * actualAccel * delta.normalize()
        // 判断高度差
        heightRelativeRange?.let {
            if (currentPosition.y - target.y < it.min) {
                // 向上移动
                next += Vec3(0.0, acceleration, 0.0)
            }
            if (currentPosition.y - target.y > it.max) {
                // 向下移动
                next -= Vec3(0.0, acceleration, 0.0)
            }
        }
        return next

    }

    /**
     * 2. 移动到某个点上 —— 飞向可能会变化的点
     *
     * @param currentPosition 实体当前绝对坐标
     * @param currentVelocity 实体当前速度向量
     * @param targetPoint 目标点（绝对世界坐标）
     * @param acceleration 加速度大小
     * @param damping 每 tick 速度衰减系数，默认 0.82
     * @param arriveDistance 到达判定距离，在此距离内刹车，默认 0.9
     */
    fun moveToPoint(
        currentPosition: Vec3,
        currentVelocity: Vec3,
        targetPoint: Vec3,
        acceleration: Double,
        damping: Double = 0.82,
        arriveDistance: Double = 0.9
    ): Vec3 {
        val delta = targetPoint.subtract(currentPosition)
        val dist = delta.length()
        if (dist <= arriveDistance) return brake(currentVelocity, damping)
        val dir = delta.scale(1.0 / dist)
        return currentVelocity.scale(damping).add(dir.scale(acceleration))
    }

    /**
     * 3. 环绕某个点 —— 以固定半径环绕中心点飞行，同时调节高度
     *
     * @param currentPosition 实体当前绝对坐标
     * @param currentVelocity 实体当前速度向量
     * @param center 环绕中心点（绝对世界坐标）
     * @param radius 环绕半径
     * @param preferredY 目标飞行高度（相对中心点的 Y 偏移，正=上方，负=下方）
     * @param acceleration 加速度大小
     * @param damping 每 tick 速度衰减系数，默认 0.84
     * @param clockwise true 为顺时针环绕，默认 false（逆时针）
     */
    fun orbitAround(
        currentPosition: Vec3,
        currentVelocity: Vec3,
        center: Vec3,
        radius: Double,
        preferredY: Double,
        acceleration: Double,
        damping: Double = 0.84,
        clockwise: Boolean = false
    ): Vec3 {
        val offset = currentPosition.subtract(center)
        val dist = offset.length().coerceAtLeast(1.0E-4)
        val horizontalOffset = Vec3(offset.x, 0.0, offset.z)
        val horizontalDir = if (horizontalOffset.lengthSqr() < 1.0E-4) {
            Vec3(1.0, 0.0, 0.0)
        } else {
            horizontalOffset.normalize()
        }
        val orbitSign = if (clockwise) -1.0 else 1.0
        val tangent = Vec3(-horizontalDir.z * orbitSign, 0.0, horizontalDir.x * orbitSign)
        val radialDir = offset.scale(1.0 / dist)
        val radialWeight = ((radius - dist) / radius).coerceIn(-0.9, 0.9)
        val verticalWeight = ((center.y + preferredY - currentPosition.y) / 6.0).coerceIn(-0.45, 0.45)
        val steering = tangent.scale(1.12)
            .add(radialDir.scale(radialWeight))
            .add(0.0, verticalWeight, 0.0)
        val normalized = if (steering.lengthSqr() < 1.0E-4) {
            Vec3(tangent.x, verticalWeight, tangent.z).normalize()
        } else {
            steering.normalize()
        }
        return currentVelocity.scale(damping).add(normalized.scale(acceleration))
    }

    /**
     * 4. 环绕靠近某个点 —— 环绕的同时逐渐靠近中心，缩小到目标半径后保持环绕
     *
     * @param currentPosition 实体当前绝对坐标
     * @param currentVelocity 实体当前速度向量
     * @param center 环绕中心点（绝对世界坐标）
     * @param targetRadius 最终目标环绕半径，到达后不再缩小
     * @param preferredY 目标飞行高度（相对中心点的 Y 偏移，正=上方，负=下方）
     * @param acceleration 加速度大小
     * @param damping 每 tick 速度衰减系数，默认 0.84
     * @param shrinkRate 每 tick 半径缩小量，默认 0.02
     * @param clockwise true 为顺时针环绕，默认 false（逆时针）
     */
    fun orbitApproach(
        currentPosition: Vec3,
        currentVelocity: Vec3,
        center: Vec3,
        targetRadius: Double,
        preferredY: Double,
        acceleration: Double,
        damping: Double = 0.84,
        shrinkRate: Double = 0.02,
        clockwise: Boolean = false
    ): Vec3 {
        val safeTargetRadius = targetRadius.coerceAtLeast(0.0)
        val horizontalOffset = Vec3(
            currentPosition.x - center.x,
            0.0,
            currentPosition.z - center.z
        )
        val horizontalDistance = horizontalOffset.length()
        val horizontalVelocity = Vec3(currentVelocity.x, 0.0, currentVelocity.z)
        val outwardDir = when {
            horizontalDistance > 1.0E-4 -> horizontalOffset.scale(1.0 / horizontalDistance)
            horizontalVelocity.lengthSqr() > 1.0E-4 -> horizontalVelocity.normalize()
            else -> Vec3(1.0, 0.0, 0.0)
        }
        val orbitSign = if (clockwise) -1.0 else 1.0
        val tangent = Vec3(-outwardDir.z * orbitSign, 0.0, outwardDir.x * orbitSign)
        val radialError = horizontalDistance - safeTargetRadius
        val maxApproachSpeed = maxOf(shrinkRate, acceleration * 0.75, 0.05)
        val radialSpeed = when {
            radialError > 0.0 -> -radialError.coerceAtMost(maxApproachSpeed)
            radialError < -0.1 -> (-radialError).coerceAtMost(maxApproachSpeed * 0.6)
            else -> 0.0
        }
        val baseTangentSpeed = acceleration * if (radialError > 0.0) 0.75 else 0.9
        val tangentSpeed = if (radialError > 0.0) {
            baseTangentSpeed.coerceAtMost((-radialSpeed) * 0.65)
        } else {
            baseTangentSpeed
        }
        val verticalSpeed = ((center.y + preferredY - currentPosition.y) * 0.22)
            .coerceIn(-maxApproachSpeed, maxApproachSpeed)
        val desiredVelocity = outwardDir.scale(radialSpeed)
            .add(tangent.scale(tangentSpeed))
            .add(0.0, verticalSpeed, 0.0)
        val steering = desiredVelocity.subtract(currentVelocity)
        var nextVelocity = currentVelocity
            .add(steering.limitLength(acceleration.coerceAtLeast(0.05)))
            .scale(damping)

        if (radialError > 0.0) {
            val minInwardSpeed = radialError.coerceAtMost(maxApproachSpeed) * 0.65
            val radialComponent = nextVelocity.dot(outwardDir)
            if (radialComponent > -minInwardSpeed) {
                nextVelocity = nextVelocity.add(outwardDir.scale(-minInwardSpeed - radialComponent))
            }

            val correctedRadialComponent = nextVelocity.dot(outwardDir)
            val horizontalNext = Vec3(nextVelocity.x, 0.0, nextVelocity.z)
            val tangentComponent = horizontalNext.subtract(outwardDir.scale(correctedRadialComponent))
            val tangentLimit = (-correctedRadialComponent).coerceAtLeast(0.0) * 0.65
            if (tangentComponent.lengthSqr() > tangentLimit * tangentLimit) {
                val limitedTangent = tangentComponent.limitLength(tangentLimit)
                val correctedHorizontal = outwardDir.scale(correctedRadialComponent).add(limitedTangent)
                nextVelocity = Vec3(correctedHorizontal.x, nextVelocity.y, correctedHorizontal.z)
            }
        }

        val maxSpeed = maxOf(acceleration * 2.0, maxApproachSpeed * 1.4, 0.2)
        return nextVelocity.limitLength(maxSpeed)
    }
}
