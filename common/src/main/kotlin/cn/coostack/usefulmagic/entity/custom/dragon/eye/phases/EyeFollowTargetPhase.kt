package cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.boxCenterPosition
import kotlin.math.absoluteValue
import kotlin.math.sin

class EyeFollowTargetPhase : PhaseDefinition<MagicSubEyeEntity> {
    companion object {
        const val ID = "eye-follow-target-phase"
        val HOLDER = PhaseRegistries.register(ID) { EyeFollowOwnerPhase() }
    }

    override fun canBegin(instance: MagicSubEyeEntity, runtime: PhaseRuntime): Boolean {
        return instance.target != null && (instance.target != null || !instance.skillManager.hasActiveSkill())
    }

    override fun begin(instance: MagicSubEyeEntity, runtime: PhaseRuntime) {
    }

    override fun step(instance: MagicSubEyeEntity, runtime: PhaseRuntime): PhaseResult {
        val target = instance.target?.boxCenterPosition() ?: return PhaseResult.Next(EyeFollowOwnerPhase())

        val followRatioSin = sin(instance.tickCount * 5 / 180.0).absoluteValue

        instance.deltaMovement = FlightMovementUtil.maintainOffset(
            currentPosition = instance.positionOnEye(),
            currentVelocity = instance.deltaMovement,
            target = target,
            acceleration = 0.35,
            damping = 0.82,
            8.0 + 4 * followRatioSin minRangeTo 12.0 + 5 * followRatioSin,
            1.0 minRangeTo (2.0 + 2 * followRatioSin)
        )

        return PhaseResult.Continue
    }

    override fun end(instance: MagicSubEyeEntity, runtime: PhaseRuntime) {
    }

    override fun id(): String {
        return ID
    }
}