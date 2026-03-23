package cn.coostack.usefulmagic.entity.util.phases.dragon

import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.usefulmagic.entity.custom.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.asBoolean
import cn.coostack.usefulmagic.extend.asDouble
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * - 需要手动设置环绕点， 半径
 * - 结束需要手动修改Phase （强制）
 */
class DragonOrbitFlightPhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "orbit-flight-phase"
        // 环绕半径参数键，需要传入 Double，未设置时使用 radius 属性或默认值
        const val RADIUS = "radius"
        // 环绕最小半径参数键，需要传入 Double，防止半径收缩过小
        const val MIN_RADIUS = "min_radius"
        // 是否顺时针环绕的参数键，需要传入 Boolean
        const val CLOCKWISE = "clockwise"
        // 垂直方向跟随目标高度的插值参数键，需要传入 Double
        const val ORBIT_HEIGHT_LERP = "orbit_height_lerp"
        // 径向修正力度参数键，需要传入 Double，控制贴近/拉开速度
        const val RADIAL_CORRECTION_GAIN = "radial_correction_gain"
        // 径向修正的最大限制参数键，需要传入 Double
        const val MAX_RADIAL_CORRECTION = "max_radial_correction"
        // 速度方向插值系数参数键，需要传入 Double，控制环绕转向平滑度
        const val DIRECTION_LERP = "direction_lerp"

        private const val DEFAULT_RADIUS = 18.0
        private const val DEFAULT_MIN_RADIUS = 4.0
        private const val DEFAULT_ORBIT_HEIGHT_LERP = 0.12
        private const val DEFAULT_RADIAL_CORRECTION_GAIN = 0.14
        private const val DEFAULT_MAX_RADIAL_CORRECTION = 0.85
        private const val DEFAULT_DIRECTION_LERP = 0.28
        val HOLDER = PhaseRegistries.register(ID) { DragonOrbitFlightPhase() }
    }

    var center: Vec3? = null
    var radius: Double = DEFAULT_RADIUS
    var clockwise: Boolean = true

    private var orbitAngle = 0.0

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return center != null || runtime.hasTarget()
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        val orbitCenter = runtime.peekTargetOrNull() ?: center ?: return
        center = orbitCenter
        if (!runtime.hasTarget()) {
            runtime.addTarget(orbitCenter)
        }

        val offset = instance.position() - orbitCenter
        orbitAngle = if (offset.lengthSqr() <= 1.0E-6) {
            0.0
        } else {
            atan2(offset.z, offset.x)
        }
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val orbitCenter = runtime.peekTargetOrNull() ?: center
        if (orbitCenter == null) {
            instance.deltaMovement = instance.deltaMovement.scale(0.8)
            return PhaseResult.Continue
        }

        center = orbitCenter
        val minRadius = (runtime.params[MIN_RADIUS].asDouble ?: DEFAULT_MIN_RADIUS).coerceAtLeast(0.0)
        val safeRadius = (runtime.params[RADIUS].asDouble ?: radius).coerceAtLeast(minRadius)
        val horizontalOffset = Vec3(
            instance.x - orbitCenter.x,
            0.0,
            instance.z - orbitCenter.z
        )
        val radialDirection = if (horizontalOffset.lengthSqr() <= 1.0E-6) {
            Vec3(cos(orbitAngle), 0.0, sin(orbitAngle))
        } else {
            orbitAngle = atan2(horizontalOffset.z, horizontalOffset.x)
            horizontalOffset.normalize()
        }
        val orbitClockwise = runtime.params[CLOCKWISE].asBoolean ?: clockwise
        val tangentDirection = if (orbitClockwise) {
            Vec3(-radialDirection.z, 0.0, radialDirection.x)
        } else {
            Vec3(radialDirection.z, 0.0, -radialDirection.x)
        }
        val currentRadius = horizontalOffset.length()
        val radialCorrection =
            (safeRadius - currentRadius) *
                    (runtime.params[RADIAL_CORRECTION_GAIN].asDouble ?: DEFAULT_RADIAL_CORRECTION_GAIN)
        val maxRadialCorrection =
            (runtime.params[MAX_RADIAL_CORRECTION].asDouble ?: DEFAULT_MAX_RADIAL_CORRECTION).coerceAtLeast(0.0)
        val desiredDirection = tangentDirection
            .add(radialDirection.scale(radialCorrection.coerceIn(-maxRadialCorrection, maxRadialCorrection)))
            .add(
                0.0,
                (orbitCenter.y - instance.y) *
                        (runtime.params[ORBIT_HEIGHT_LERP].asDouble ?: DEFAULT_ORBIT_HEIGHT_LERP),
                0.0
            )

        val currentVelocity = instance.deltaMovement
        val flightSpeed = instance.getAttributeValue(Attributes.FLYING_SPEED)
        val normalizedVelocity = if (currentVelocity.lengthSqr() <= 1.0E-6) {
            tangentDirection.normalize()
        } else {
            currentVelocity.normalize()
        }
        val desiredVelocity = if (desiredDirection.lengthSqr() <= 1.0E-6) {
            tangentDirection
        } else {
            desiredDirection.normalize()
        }
        val finalVelocity = GraphMathHelper.lerp(
            (runtime.params[DIRECTION_LERP].asDouble ?: DEFAULT_DIRECTION_LERP).coerceIn(0.0, 1.0),
            normalizedVelocity,
            desiredVelocity
        ).normalize().scale(flightSpeed)

        EntityPoseUtil.rotationFix(instance.forward, finalVelocity, instance, 14f)
        instance.deltaMovement = finalVelocity
        instance.addDeltaMovement(EntityPoseUtil.stayAwayFloor(instance.level(), instance.position(), finalVelocity))
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
