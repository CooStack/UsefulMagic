package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity
import java.util.ArrayDeque

class EntityQueueSkillManager(owner: LivingEntity) : EntitySkillManager(owner) {
    private val skillQueue = ArrayDeque<String>()

    override fun addSkill(skill: Skill<out LivingEntity>) {
        super.addSkill(skill)
        rebuildQueue()
    }

    /**
     * 从技能池中选择可触发且 chance 最高的技能。
     * chance 相同时保留队列顺序。
     */
    override fun choiceSkill(): Skill<LivingEntity>? {
        val skillID = skillQueue
            .asSequence()
            .mapNotNull { id ->
                val skill = skills[id] ?: return@mapNotNull null
                if (canTriggerSkill(skill)) id to skill else null
            }
            .maxByOrNull {
                it.second.chance
            }?.first ?: return null

        removeFromQueue(skillID)
        return skills[skillID]
    }

    override fun hasAnySkillToChoice(): Boolean {
        return skillQueue.any {
            val skill = skills[it] ?: return@any false
            canTriggerSkill(skill)
        }
    }

    override fun onSkillAdded(skill: Skill<LivingEntity>) {
        removeFromQueue(skill.getSkillID())
    }

    override fun onSkillCountdown(skill: Skill<LivingEntity>) {
        removeFromQueue(skill.getSkillID())
    }

    override fun onSkillCooldownFinished(id: String, skill: Skill<LivingEntity>?) {
        if (skill != null) {
            enqueueLast(skill)
        }
    }

    override fun onActiveSkillSet(skill: Skill<LivingEntity>) {
        removeFromQueue(skill.getSkillID())
    }

    override fun onActiveSkillReset(skill: Skill<LivingEntity>, release: Boolean) {
        if (!release && !hasCD(skill.getSkillID())) {
            enqueueLast(skill)
        }
    }

    private fun rebuildQueue() {
        skillQueue.clear()
        skills.values.sortedByDescending {
            it.chance
        }.forEach {
            enqueueLast(it)
        }
    }

    private fun enqueueLast(skill: Skill<LivingEntity>) {
        val skillID = skill.getSkillID()
        removeFromQueue(skillID)
        skillQueue.addLast(skillID)
    }

    private fun removeFromQueue(skillID: String) {
        skillQueue.removeAll {
            it == skillID
        }
    }
}
