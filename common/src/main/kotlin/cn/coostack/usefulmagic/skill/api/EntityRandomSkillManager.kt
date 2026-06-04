package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity
import kotlin.random.Random

class EntityRandomSkillManager(owner: LivingEntity) : EntitySkillManager(owner) {
    private val random = Random(System.currentTimeMillis())

    /**
     * 根据权重随机选择一个技能
     */
    override fun choiceSkill(): Skill<LivingEntity>? {
        return skills.asSequence()
            .filter {
                canTriggerSkill(it.value)
            }
            .maxByOrNull {
                random.nextInt(100) * it.value.chance
            }?.value
    }
}
