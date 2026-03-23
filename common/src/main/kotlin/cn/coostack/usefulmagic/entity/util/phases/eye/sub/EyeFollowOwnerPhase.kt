package cn.coostack.usefulmagic.entity.util.phases.eye.sub

import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

class EyeFollowOwnerPhase : PhaseDefinition<MagicSubEyeEntity> {
    companion object {
        const val ID = "eye-follow-owner-phase"
        val HOLDER = PhaseRegistries.register(ID) { EyeFollowOwnerPhase() }
    }

    override fun canBegin(instance: MagicSubEyeEntity, runtime: PhaseRuntime): Boolean {
        instance.resolveOwner()
        return instance.owner != null && (instance.target != null || !instance.skillManager.hasActiveSkill())
    }

    override fun begin(instance: MagicSubEyeEntity, runtime: PhaseRuntime) {
    }

    override fun step(instance: MagicSubEyeEntity, runtime: PhaseRuntime): PhaseResult {
        instance.tickFollowOwnerPhase()
        if (instance.target != null) {
            return PhaseResult.Next(EyeFollowTargetPhase())
        }
        return PhaseResult.Continue
    }

    override fun end(instance: MagicSubEyeEntity, runtime: PhaseRuntime) {
    }

    override fun id(): String {
        return ID
    }
}
