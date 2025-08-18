package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity

/**
 * 触发此技能的条件
 */
interface SkillCondition {
    fun canTrigger(entity: LivingEntity): Boolean
}