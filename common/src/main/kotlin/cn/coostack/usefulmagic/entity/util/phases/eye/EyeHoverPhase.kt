package cn.coostack.usefulmagic.entity.util.phases.eye

import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

/**
 * 悬浮行为 手动设置target
 */
class EyeHoverPhase : PhaseDefinition<MagicEyeEntity> {
    companion object {
        const val ID = "EyeHoverPhase"
        val HOLDER = PhaseRegistries.register(ID) { EyeHoverPhase() }
    }

    override fun canBegin(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return true
    }

    override fun begin(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun step(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        if (!runtime.hasTarget()) {
            return PhaseResult.Reset
        }
        val target = runtime.peekTarget().get()
        if (target.distanceTo(instance.positionOnEye()) <= 1) {
            val nextVelocity = FlightMovementUtil.brake(instance.deltaMovement)
            instance.deltaMovement = nextVelocity
        } else {
            val nextVelocity = FlightMovementUtil.moveToPoint(
                instance.positionOnEye(),
                instance.deltaMovement,
                target,
                0.8,
                arriveDistance = 0.5
            )
            instance.deltaMovement = nextVelocity
        }
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun id(): String {
        return ID
    }
}