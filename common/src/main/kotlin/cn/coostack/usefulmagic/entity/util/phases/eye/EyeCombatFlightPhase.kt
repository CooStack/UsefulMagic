package cn.coostack.usefulmagic.entity.util.phases.eye

import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

class EyeCombatFlightPhase : PhaseDefinition<MagicEyeEntity> {
    companion object {
        const val ID = "eye-combat-flight-phase"
        val HOLDER = PhaseRegistries.register(ID) { EyeCombatFlightPhase() }
    }

    override fun canBegin(instance: MagicEyeEntity, runtime: PhaseRuntime): Boolean {
        return true
    }

    override fun begin(instance: MagicEyeEntity, runtime: PhaseRuntime) {
    }

    override fun step(instance: MagicEyeEntity, runtime: PhaseRuntime): PhaseResult {
        instance.tickCombatFlightPhase()
        return PhaseResult.Continue
    }

    override fun end(instance: MagicEyeEntity, runtime: PhaseRuntime) {
    }

    override fun id(): String {
        return ID
    }
}

