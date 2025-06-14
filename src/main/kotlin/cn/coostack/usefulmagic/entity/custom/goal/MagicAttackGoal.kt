package cn.coostack.usefulmagic.entity.custom.goal

import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.barrages.EntityDamagedBarrage
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import java.util.EnumSet

class MagicAttackGoal(
    val entity: MagicBookEntity,
    val internal: Int,
) : Goal() {
    override fun canStart(): Boolean {
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
        entity.lookControl.lookAt(target)
        if (entity.skillManager.hasActiveSkill()) {
            return
        }
        val skill = entity.skillManager.choiceSkill() ?: let {
            if (tick++ > internal) {
                tick = 0
                entity.shootAt(target, 0f)
                entity.world.playSound(
                    null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.HOSTILE, 5f, 2f
                )
            }
            return
        }
        entity.skillManager.setActiveSkill(skill)
    }
}