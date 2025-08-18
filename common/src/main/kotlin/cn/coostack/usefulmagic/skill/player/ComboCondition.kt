package cn.coostack.usefulmagic.skill.player

import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

interface ComboCondition : SkillCondition, Comparable<ComboCondition> {
    val triggerComboMin: Int

    override fun canTrigger(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return false
        }
        val state = ComboUtil.getComboState(entity.uuid)
        return state.count >= triggerComboMin
    }

    override fun compareTo(other: ComboCondition): Int {
        return this.triggerComboMin - other.triggerComboMin
    }
}