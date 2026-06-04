package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

/**
 * 啥都不做
 *
 * @constructor Create empty None phase
 */
class DragonNonePhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "none"
        val HOLDER = PhaseRegistries.register(ID) { DragonNonePhase() }
    }

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return true
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun id(): String {
        return ID
    }
}
