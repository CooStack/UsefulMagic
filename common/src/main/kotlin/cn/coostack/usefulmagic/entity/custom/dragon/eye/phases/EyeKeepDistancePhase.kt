package cn.coostack.usefulmagic.entity.custom.dragon.eye.phases

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.asBoolean
import cn.coostack.usefulmagic.extend.asDouble

class EyeKeepDistancePhase : PhaseDefinition<MagicEyeEntity> {
    companion object {
        const val ID = "EyeKeepDistancePhase"
        val HOLDER = PhaseRegistries.register(ID) { EyeKeepDistancePhase() }

        const val DAMPING = "damping"
        const val ACCELERATION = "acceleration"
        const val MIN_CATCH_UP = "min_catch_up"
        const val MAX_CATCH_UP = "max_catch_up"

        const val HIGHER_THAN_TARGET = "higher_than_targe"
        const val HEIGHT_RELATIVE_LEAST = "height_relative_least"
        const val HEIGHT_RELATIVE_MOST = "height_relative_most"
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

        val damping = runtime.params[DAMPING].asDouble ?: 0.9
        val acceleration = runtime.params[ACCELERATION].asDouble ?: 0.8
        val minCatchUp = runtime.params[MIN_CATCH_UP].asDouble ?: 1.0
        val maxCatchUp = runtime.params[MAX_CATCH_UP].asDouble ?: 2.0

        val higherThanTarget = runtime.params[HIGHER_THAN_TARGET].asBoolean ?: false
        val heightRelativeLeast = runtime.params[HEIGHT_RELATIVE_LEAST].asDouble ?: 3.0
        val heightRelativeMost = runtime.params[HEIGHT_RELATIVE_MOST].asDouble ?: 7.0
        val target = runtime.peekTarget().get()
        val nextVelocity = FlightMovementUtil.maintainOffset(
            instance.positionOnEye(),
            instance.deltaMovement,
            target,
            acceleration,
            damping,
            minCatchUp minRangeTo maxCatchUp,
        )
        if (higherThanTarget) heightRelativeLeast minRangeTo heightRelativeMost else null
        instance.deltaMovement = nextVelocity
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