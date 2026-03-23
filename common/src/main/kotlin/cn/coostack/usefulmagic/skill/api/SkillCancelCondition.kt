package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity

interface SkillCancelCondition<T: LivingEntity>: SkillCancelable {
    /**
     * @return true 中断技能施放
     */
    fun testCancel(entity: T): Boolean
}