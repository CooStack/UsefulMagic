package cn.coostack.usefulmagic.entity.custom.goal.dragon

import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import net.minecraft.world.entity.ai.goal.Goal

class MagicDragonAttackGoal(val mob: MagicDragonEntity) : Goal() {
    override fun canUse(): Boolean {
        return mob.target != null && !mob.entityDeath && mob.dizzinessTick <= 0 && mob.animateTicking == 0
    }

    override fun tick() {
        if (mob.entityDeath) return

        if (mob.dizzinessTick > 0) return

        mob.target ?: let {
            mob.skillManager.resetActiveSkill()
            return
        }
        if (mob.skillManager.hasActiveSkill()) {
            return
        }
        val skill = mob.skillManager.choiceSkill() ?: return
        mob.skillManager.setActiveSkill(skill)
        super.tick()
    }
}
