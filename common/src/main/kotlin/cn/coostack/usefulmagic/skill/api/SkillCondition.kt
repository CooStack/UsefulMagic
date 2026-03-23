package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity

/**
 * 触发此技能的条件
 */
interface SkillCondition<T: LivingEntity> {
    /**
     * 能否触发该技能
     *
     * @param entity 触发技能的实体
     * @return
     */
    fun canTrigger(entity: T): Boolean
}