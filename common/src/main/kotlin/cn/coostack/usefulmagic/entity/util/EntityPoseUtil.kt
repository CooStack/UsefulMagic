package cn.coostack.usefulmagic.entity.util

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * 用于计算当前tick的yaw 朝向等内容
 *
 */
object EntityPoseUtil {
    private fun getHorizontalYawDegrees(direction: Vec3): Float {
        return Math3DUtil.getYawFromLocation(direction).toFloat() * 180F / PIF
    }

    private fun wrapDegrees(degrees: Float): Float {
        var wrapped = degrees % 360F
        if (wrapped >= 180F) {
            wrapped -= 360F
        }
        if (wrapped < -180F) {
            wrapped += 360F
        }
        return wrapped
    }

    private fun approachYaw(current: Float, target: Float, maxStep: Float): Float {
        val delta = wrapDegrees(target - current)
        if (delta.absoluteValue < maxStep) {
            return wrapDegrees(current + delta)
        }
        return wrapDegrees(current + maxStep * delta.sign)
    }

    /**
     * 插值旋转
     *
     * @param direction 当前方向
     * @param targetDirection 目标方向
     * @param maxSpeed 最大旋转速度 角度
     * @return 实体的yaw的增值
     */
    fun lerpRotationFromDirection(direction: Vec3, targetDirection: Vec3, maxSpeed: Float): Float {
        // 只考虑水平的情况
        val current = getHorizontalYawDegrees(direction)
        val target = getHorizontalYawDegrees(targetDirection)
        return approachYaw(current, target, maxSpeed)
    }

    /**
     * 插值旋转
     *
     * @param direction 当前方向
     * @param targetDirection 目标方向
     * @param speed 最大旋转速度 角度
     * @return 实体的yaw的增值
     */
    fun lerpRotationFromDirectionAsSpeed(direction: Vec3, targetDirection: Vec3, speed: Float): Float {
        // 只考虑水平的情况
        val current = getHorizontalYawDegrees(direction)
        val target = getHorizontalYawDegrees(targetDirection)
        return approachYaw(current, target, speed)
    }


    fun stayAwayFloor(world: Level, pos: Vec3, velocity: Vec3): Vec3 {
        // 判断下方是否是方块， 是的话就往上飞, 然后可以穿过方块(向前但是不能向下）
        val type = world.clip(
            ClipContext(
                pos, pos + Vec3(0.0, velocity.y, 0.0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY,
                CollisionContext.empty()
            )
        )
        if (type != null && type.type != HitResult.Type.MISS) {
            // 向上匀速飞
            return Vec3(0.0, 1.0, 0.0)
        }
        return Vec3.ZERO
    }

    fun getVelocityAirDragRatio(currentSpeed: Double): Double {
        // 空气阻力随速度平方增长，低速时轻微，高速时更明显
        val airDragFactor = (1.0 - currentSpeed * currentSpeed * 0.004).coerceIn(0.0, 1.0)
        return airDragFactor
    }

    fun rotationFix(forward: Vec3, targetDirection: Vec3, instance: LivingEntity, speed: Float = 7f) {
        // 修改yaw
        val nextYaw = lerpRotationFromDirectionAsSpeed(
            forward, targetDirection, speed
        )

        instance.yRotO = instance.yRot
        instance.yRot = nextYaw
    }

}
