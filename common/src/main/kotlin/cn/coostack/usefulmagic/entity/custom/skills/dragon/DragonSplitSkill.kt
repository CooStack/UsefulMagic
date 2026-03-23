package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity

/**
 * 一口气召唤3个 召唤10次
 *
 * 此技能释放时， 龙悬浮在spawnPosition上
 */
class DragonSplitSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_split_skill"
    }

    override var chance: Double = 0.0
    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 28
    }


    override fun onActive(source: MagicDragonEntity) {
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 0
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }
}
