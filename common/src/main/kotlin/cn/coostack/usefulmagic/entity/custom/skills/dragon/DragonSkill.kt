package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition

abstract class DragonSkill : Skill<MagicDragonEntity>, SkillCancelCondition<MagicDragonEntity>,
    SkillCondition<MagicDragonEntity> {
    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
    override fun testCancel(entity: MagicDragonEntity): Boolean {
        return !canTrigger(entity)
    }

    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return !entity.entityDeath && entity.dizzinessTick <= 0 && !UsefulMagicEffects.isMagicSealed(entity)
    }
}