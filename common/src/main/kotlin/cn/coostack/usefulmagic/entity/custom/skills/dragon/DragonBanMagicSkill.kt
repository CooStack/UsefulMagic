package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.effects.asHolder
import cn.coostack.usefulmagic.entity.custom.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

/**
 * 一个中场面，  释放后会让周围的玩家有60秒的魔力禁止时间
 * 此技能为蓄力技能
 * 会先以spawnPosition为中心释放一个大魔法阵，然后龙会环绕接近（Phase）
 *
 * 最后转换为Hover 技能结束的时候才会转换为Simple
 */
class DragonBanMagicSkill : DragonSkill() {
    override var chance: Double = 0.18
    private var sealedTarget: LivingEntity? = null

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 18 * 20
    }

    override fun onActive(source: MagicDragonEntity) {
        sealedTarget = source.getCombatTarget()
        source.playAnimation(MagicDragonAnimationState.OPEN_MOUTH)
        source.guideBreathDive(sealedTarget ?: return)
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {

        // 这里要进行 explode 然后给128范围内的所有实体设置 1分钟的 禁魔
        val world = source.level()
        world.getEntitiesOfClass(LivingEntity::class.java, source.boundingBox.inflate(128.0)) {
            it.isAlive
        }.forEach {
            it.addEffect(MobEffectInstance(UsefulMagicEffects.MAGIC_SEALED.asHolder(), 20 * 60))
        }

        // play particles

        // play sound

        stopHolding(source, holdingTick)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 30
    }

    override fun holdingTick(holdingEntity: MagicDragonEntity, holdTicks: Int) {
        if (holdTicks > 10) {
            sealedTarget?.takeIf { it.isAlive && it.distanceToSqr(holdingEntity) <= 16.0 * 16.0 }?.let {
                it.hurt(it.damageSources().mobAttack(holdingEntity), 8f)
                it.deltaMovement = holdingEntity.createImpactVelocity(holdingEntity.getBreathAimDirection(), 0.35, 0.8)
                it.hurtMarked = true
                stopHolding(holdingEntity, holdTicks)
            }
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        sealedTarget = null
        entity.stopBreathDive()
        entity.clearSkillAnimation()
    }

    override fun getSkillID(): String {
        return "dragon_ban_magic_skill"
    }

    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        val target = entity.getCombatTarget() ?: return false
        return !entity.isRamAttacking() && target.distanceToSqr(entity) <= 18.0 * 18.0
    }
}
