package cn.coostack.usefulmagic.entity.util.phases

import net.minecraft.world.entity.LivingEntity


sealed interface PhaseResult {
    object Continue : PhaseResult
    object Reset : PhaseResult
    data class Complete(val data: Map<String, Any> = mapOf()) : PhaseResult
    data class Next(
        val phase: PhaseDefinition<out LivingEntity>,
        val inheritTargets: Boolean = false,
        val setup: (PhaseRuntime.() -> Unit) = {}
    ) : PhaseResult
}
