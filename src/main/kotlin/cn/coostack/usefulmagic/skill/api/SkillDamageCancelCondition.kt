package cn.coostack.usefulmagic.skill.api

import net.minecraft.entity.LivingEntity

/**
 * 因伤害而中断
 */
interface SkillDamageCancelCondition : SkillCancelCondition {
    var damageAmount: Float

    fun damage(amount: Float) {
        damageAmount += amount
    }

    fun maxDamage(entity: LivingEntity): Float

    override fun testCancel(entity: LivingEntity): Boolean {
        return damageAmount >= maxDamage(entity)
    }

}