package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.asBoolean
import cn.coostack.usefulmagic.extend.asDouble

/**
 * 看着目标 + 保持距离
 *
 * 参考 [cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeKeepDistancePhase]c
 * 使用 [FlightMovementUtil.maintainOffset] 实现：
 * - 距离区间内刹车
 * - 距离 < min 远离 / 距离 > max 靠近
 * - 可选 Y 轴高度差范围限制
 * - 同时朝向目标平滑转向
 *
 * 由 [DragonImpactPhase] 之类的阶段切入，作为持续追踪 / 牵制的姿态。
 */
class DragonLookAndKeepDistancePhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "dragon-look-and-keep-distance-phase"
        val HOLDER = PhaseRegistries.register(ID) { DragonLookAndKeepDistancePhase() }

        /** 速度阻尼系数 (Double)，每 tick 速度衰减比例，越大越平滑 */
        const val DAMPING = "damping"

        /** 加速度大小 (Double)，决定跟随 / 追击速度 */
        const val ACCELERATION = "acceleration"

        /** 保持距离区间最小值 (Double)，距离小于 `min` 时远离目标 */
        const val MIN_CATCH_UP = "min_catch_up"

        /** 保持距离区间最大值 (Double)，距离大于 `max` 时靠近目标 */
            const val MAX_CATCH_UP = "max_catch_up"

        /** 是否启用 Y 轴高度差限制 (Boolean)，开启后会保持自身高度差在 `[HEIGHT_RELATIVE_LEAST, HEIGHT_RELATIVE_MOST]` 之间 */
        const val LIMIT_HEIGHT = "limit_height"

        /** 自身相对目标 Y 轴差的最小值 (Double)，`currentY - targetY` 小于该值时向上修正 */
        const val HEIGHT_RELATIVE_LEAST = "height_relative_least"

        /** 自身相对目标 Y 轴差的最大值 (Double)，`currentY - targetY` 大于该值时向下修正 */
        const val HEIGHT_RELATIVE_MOST = "height_relative_most"

        /** 进入阶段后丢失目标的最大持续 tick (Int)，超过则返回 [PhaseResult.Reset] */
        const val LOST_TARGET_TIMEOUT = "lost_target_timeout"

        private const val DEFAULT_DAMPING = 0.9
        private const val DEFAULT_ACCELERATION = 0.8
        private const val DEFAULT_MIN_CATCH_UP = 10.0
        private const val DEFAULT_MAX_CATCH_UP = 18.0
        private const val DEFAULT_LIMIT_HEIGHT = true
        private const val DEFAULT_HEIGHT_RELATIVE_LEAST = 4.0
        private const val DEFAULT_HEIGHT_RELATIVE_MOST = 10.0
        private const val DEFAULT_LOST_TARGET_TIMEOUT = 40
    }

    private var lostTargetTick = 0

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return runtime.hasTarget() || instance.getCombatTarget() != null
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        lostTargetTick = 0
        if (!runtime.hasTarget()) {
            instance.getCombatTarget()?.let { runtime.addTarget(it.position()) }
        }
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val combat = instance.getCombatTarget()
        if (combat != null) {
            val livePos = combat.position()
            if (runtime.hasTarget()) {
                runtime.setCurrentTarget(livePos)
            } else {
                runtime.addTarget(livePos)
            }
            lostTargetTick = 0
        } else if (!runtime.hasTarget()) {
            lostTargetTick++
            val timeout = runtime.params[LOST_TARGET_TIMEOUT].asDouble?.toInt() ?: DEFAULT_LOST_TARGET_TIMEOUT
            if (lostTargetTick > timeout) {
                return PhaseResult.Reset
            }
            instance.deltaMovement = FlightMovementUtil.brake(instance.deltaMovement, 0.9)
            instance.playAnimation(MagicDragonAnimationState.FLY)
            return PhaseResult.Continue
        }

        val target = runtime.peekTargetOrNull() ?: return PhaseResult.Reset

        val damping = runtime.params[DAMPING].asDouble ?: DEFAULT_DAMPING
        val acceleration = runtime.params[ACCELERATION].asDouble ?: DEFAULT_ACCELERATION
        val minCatchUp = runtime.params[MIN_CATCH_UP].asDouble ?: DEFAULT_MIN_CATCH_UP
        val maxCatchUp = (runtime.params[MAX_CATCH_UP].asDouble ?: DEFAULT_MAX_CATCH_UP)
            .coerceAtLeast(minCatchUp)

        val limitHeight = runtime.params[LIMIT_HEIGHT].asBoolean ?: DEFAULT_LIMIT_HEIGHT
        val heightRelativeLeast =
            runtime.params[HEIGHT_RELATIVE_LEAST].asDouble ?: DEFAULT_HEIGHT_RELATIVE_LEAST
        val heightRelativeMost = (runtime.params[HEIGHT_RELATIVE_MOST].asDouble ?: DEFAULT_HEIGHT_RELATIVE_MOST)
            .coerceAtLeast(heightRelativeLeast)

        val position = instance.position()
        val nextVelocity = FlightMovementUtil.maintainOffset(
            position,
            instance.deltaMovement,
            target,
            acceleration,
            damping,
            minCatchUp minRangeTo maxCatchUp,
            if (limitHeight) heightRelativeLeast minRangeTo heightRelativeMost else null
        )
        instance.deltaMovement = nextVelocity

        val toTarget = target.subtract(position)
        if (toTarget.lengthSqr() > 1.0E-6) {
            // 直接吸附朝向：用足够大的旋转速度让插值一次跨过差值
            EntityPoseUtil.rotationFix(
                instance.forward,
                toTarget,
                instance,
                360f
            )
        }

        instance.addDeltaMovement(
            EntityPoseUtil.stayAwayFloor(instance.level(), position, nextVelocity)
        )
        instance.playAnimation(MagicDragonAnimationState.FLY)
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        runtime.clearTargets()
        lostTargetTick = 0
    }

    override fun id(): String = ID
}
