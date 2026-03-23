package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity

/**
 * 因伤害而中断
 */
interface SkillDamageCancelCondition<T: LivingEntity> : SkillCancelCondition<T> {
    var damageAmount: Float

    fun damage(amount: Float) {
        damageAmount += amount
    }

    fun maxDamage(entity: T): Float

    override fun testCancel(entity: T): Boolean {
        return damageAmount >= maxDamage(entity)
    }

}