package cn.coostack.usefulmagic.entity.util.phases.eye.sub

import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

class EyeSuicidePhase : PhaseDefinition<MagicSubEyeEntity> {
    companion object {
        const val ID = "eye-suicide-phase"
        val HOLDER = PhaseRegistries.register(ID) { EyeSuicidePhase() }
    }

    override fun canBegin(
        instance: MagicSubEyeEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return true
    }

    override fun begin(
        instance: MagicSubEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun step(
        instance: MagicSubEyeEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val target = runtime.peekTargetOrNull() ?: return PhaseResult.Reset
        instance.deltaMovement = FlightMovementUtil.moveToPoint(
            instance.position(),
            instance.deltaMovement,
            target,
            0.3, 0.7, 0.5
        )
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicSubEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun id(): String {
        return ID
    }
}