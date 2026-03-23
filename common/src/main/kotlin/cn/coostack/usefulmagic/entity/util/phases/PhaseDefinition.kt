package cn.coostack.usefulmagic.entity.util.phases

import net.minecraft.world.entity.LivingEntity

/**
 * 不同的移动方式 （用技能进行操作）
 *
 * @constructor Create empty Movement phase
 */
interface PhaseDefinition<T : LivingEntity> {

    fun canBegin(instance: T, runtime: PhaseRuntime): Boolean

    fun begin(instance: T, runtime: PhaseRuntime)

    fun step(instance: T, runtime: PhaseRuntime): PhaseResult

    /**
     * 会继承target列表， 如果不需要，那就要在这里clear
     *
     * @param instance
     * @param runtime
     */
    fun end(instance: T, runtime: PhaseRuntime)

    fun id(): String
}
