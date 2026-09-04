package cn.coostack.usefulmagic.entity.custom.dragon.goal

import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import net.minecraft.world.entity.ai.goal.Goal

class MagicDragonAttackGoal(val mob: MagicDragonEntity) : Goal() {
    override fun canUse(): Boolean {
        return mob.target != null && !mob.isDizzying() && !mob.isSpawnShoutLocked() && mob.animateTicking <= 0
    }

    override fun tick() {
        if (mob.isSpawnShoutLocked()) return
        if (mob.isDizzying()) return
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
