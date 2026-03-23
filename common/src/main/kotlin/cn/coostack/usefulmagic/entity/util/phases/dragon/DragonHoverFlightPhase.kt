package cn.coostack.usefulmagic.entity.util.phases.dragon

import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.asDouble
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3

/**
 * - 手动设置， 移动到target然后悬停 移动方式和DragonSimpleFlightPhase一致
 * - 手动结束
 * @constructor Create empty Hover flight phase
 */
class DragonHoverFlightPhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "hover-flight-phase"
        // 进入悬停判定的半径参数键，需要传入 Double
        const val HOVER_ARRIVE_RADIUS = "hover_arrive_radius"
        // 悬停状态下最低保留飞行速度参数键，需要传入 Double
        const val HOVER_KEEP_SPEED = "hover_keep_speed"
        // 悬停到目标附近时的修正力度参数键，需要传入 Double
        const val HOVER_CORRECTION_SPEED = "hover_correction_speed"
        // 没有目标时的减速倍率参数键，需要传入 Double
        const val HOVER_IDLE_DRAG = "hover_idle_drag"
        // 进入悬停后拖拽下限参数键，需要传入 Double
        const val HOVER_DRAG_MIN = "hover_drag_min"
        // 进入悬停后整体拖拽倍率参数键，需要传入 Double
        const val HOVER_DRAG_SCALE = "hover_drag_scale"
        // 悬停时额外向上的微小速度参数键，需要传入 Double
        const val HOVER_UPWARD_SPEED = "hover_upward_speed"
        // 方向插值系数参数键，需要传入 Double，控制转向平滑度
        const val DIRECTION_LERP = "direction_lerp"

        private const val DEFAULT_HOVER_ARRIVE_RADIUS = 6.0
        private const val DEFAULT_HOVER_KEEP_SPEED = 0.08
        private const val DEFAULT_HOVER_CORRECTION_SPEED = 0.12
        private const val DEFAULT_HOVER_IDLE_DRAG = 0.6
        private const val DEFAULT_HOVER_DRAG_MIN = 0.6
        private const val DEFAULT_HOVER_DRAG_SCALE = 0.75
        private const val DEFAULT_HOVER_UPWARD_SPEED = 0.02
        private const val DEFAULT_DIRECTION_LERP = 0.15
        val HOLDER = PhaseRegistries.register(ID) { DragonHoverFlightPhase() }
    }

    var target: Vec3? = null

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return target != null || runtime.hasTarget()
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        if (!runtime.hasTarget()) {
            target?.let(runtime::addTarget)
        }
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val hoverTarget = runtime.peekTargetOrNull() ?: target
        if (hoverTarget == null) {
            val idleDrag = (runtime.params[HOVER_IDLE_DRAG].asDouble ?: DEFAULT_HOVER_IDLE_DRAG).coerceAtLeast(0.0)
            instance.deltaMovement = instance.deltaMovement.scale(idleDrag)
            instance.playAnimation(MagicDragonAnimationState.FLY)
            return PhaseResult.Continue
        }

        val position = instance.position()
        val currentVelocity = instance.deltaMovement
        val targetDirection = hoverTarget - position
        val arriveRadius = (runtime.params[HOVER_ARRIVE_RADIUS].asDouble ?: DEFAULT_HOVER_ARRIVE_RADIUS)
            .coerceAtLeast(1.0E-6)
        if (Math3DUtil.isPointCrossBySphere(position, currentVelocity, arriveRadius, hoverTarget)) {
            val dragMin = (runtime.params[HOVER_DRAG_MIN].asDouble ?: DEFAULT_HOVER_DRAG_MIN).coerceAtLeast(0.0)
            val dragScale = (runtime.params[HOVER_DRAG_SCALE].asDouble ?: DEFAULT_HOVER_DRAG_SCALE).coerceAtLeast(0.0)
            val correctionSpeed =
                (runtime.params[HOVER_CORRECTION_SPEED].asDouble ?: DEFAULT_HOVER_CORRECTION_SPEED)
            val hoverCorrection = if (targetDirection.lengthSqr() <= 1.0E-6) {
                Vec3.ZERO
            } else {
                targetDirection.scale(correctionSpeed / arriveRadius)
            }
            val drag = EntityPoseUtil.getVelocityAirDragRatio(currentVelocity.length()).coerceAtLeast(dragMin)
            val upwardSpeed = runtime.params[HOVER_UPWARD_SPEED].asDouble ?: DEFAULT_HOVER_UPWARD_SPEED
            instance.deltaMovement = currentVelocity
                .scale(drag)
                .scale(dragScale)
                .add(hoverCorrection)
                .add(0.0, upwardSpeed, 0.0)
            instance.addDeltaMovement(EntityPoseUtil.stayAwayFloor(instance.level(), position, instance.deltaMovement))
            instance.playAnimation(MagicDragonAnimationState.FLY)
            return PhaseResult.Continue
        }

        EntityPoseUtil.rotationFix(instance.forward, targetDirection, instance)
        val flightSpeed = instance.getAttributeValue(Attributes.FLYING_SPEED)
        val normalizedVelocity = if (currentVelocity.lengthSqr() <= 1.0E-6) {
            instance.forward.normalize()
        } else {
            currentVelocity.normalize()
        }
        val targetVelocity = GraphMathHelper.lerp(
            (runtime.params[DIRECTION_LERP].asDouble ?: DEFAULT_DIRECTION_LERP).coerceIn(0.0, 1.0),
            normalizedVelocity,
            targetDirection.normalize()
        ).normalize().scale(
            flightSpeed.coerceAtLeast(runtime.params[HOVER_KEEP_SPEED].asDouble ?: DEFAULT_HOVER_KEEP_SPEED)
        )
        instance.deltaMovement = targetVelocity
        instance.addDeltaMovement(EntityPoseUtil.stayAwayFloor(instance.level(), position, targetVelocity))
        instance.playAnimation(MagicDragonAnimationState.FLY)
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        runtime.clearTargets()
    }

    override fun id(): String {
        return ID
    }
}
