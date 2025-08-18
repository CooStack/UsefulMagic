package cn.coostack.usefulmagic.entity.custom.goal

import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.goal.Goal

class MagicAttackGoal(
    val entity: MagicBookEntity,
    val internal: Int,
) : Goal() {
    override fun canUse(): Boolean {
        return entity.target != null
    }

    var tick = 0
    override fun tick() {
        if (entity.entitySpawning) return
        if (entity.isEntityDeath()) return
        val target = entity.target ?: let {
            entity.skillManager.resetActiveSkill()
            return
        }
        entity.lookControl.setLookAt(target)
        if (entity.skillManager.hasActiveSkill()) {
            return
        }
        val skill = entity.skillManager.choiceSkill() ?: let {
            if (tick++ > internal) {
                tick = 0
                entity.performRangedAttack(target, 0f)
                entity.level().playSound(
                    null, entity.x, entity.y, entity.z, SoundEvents.BREEZE_SHOOT, SoundSource.HOSTILE, 5f, 2f
                )
            }
            return
        }
        entity.skillManager.setActiveSkill(skill)
    }
}