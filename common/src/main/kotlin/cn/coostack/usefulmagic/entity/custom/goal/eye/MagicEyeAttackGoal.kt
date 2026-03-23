package cn.coostack.usefulmagic.entity.custom.goal.eye

import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import net.minecraft.world.entity.ai.goal.Goal

class MagicEyeAttackGoal(val mob: MagicEyeEntity) : Goal() {
    override fun canUse(): Boolean {
        val target = mob.target ?: return false
        if (!mob.canAttackWithEyeMagic(target)) {
            mob.target = null
            return false
        }
        return !mob.entitySpawning
    }


    override fun tick() {
        if (mob.entitySpawning) return

        // 这里还要判定一些内容

        val target = mob.target ?: let {
            mob.skillManager.resetActiveSkill()
            return
        }
        if (!mob.canAttackWithEyeMagic(target)) {
            mob.target = null
            mob.skillManager.resetActiveSkill()
            return
        }
        if (mob.skillManager.hasActiveSkill()) {
            return
        }
        mob.lookAt(target, 20f, 20f)
        val skill = mob.skillManager.choiceSkill() ?: return
        mob.skillManager.setActiveSkill(skill)
        super.tick()
    }

}
