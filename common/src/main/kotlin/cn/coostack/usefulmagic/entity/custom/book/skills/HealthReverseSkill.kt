package cn.coostack.usefulmagic.entity.custom.book.skills

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.ShrinkParticleEmitters
import cn.coostack.usefulmagic.particles.composition.skill.TaiChiComposition
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.skill.api.SkillDamageCancelCondition

/**
 * 抢能设定和动画
 * 抢能触发条件：生命值低于 30%
 * 触发时间 20 * 30 ticks (30秒)
 * 触发动画：产生太极形状的粒子进行旋转
 * 触发效果：实体生命值逆转 (health = maxHealth - health)
 * 中断抢能
 *     触发抢能时受到 maxHealth * 0.3 的伤害
 * 中断效果
 *     实体额外受到 50 伤害
 *     并且拥有 60秒的冷却 (20 * 60)
 *
 */
class HealthReverseSkill : Skill<MagicBookEntity>, SkillCondition<MagicBookEntity>, SkillDamageCancelCondition<MagicBookEntity> {
    override var chance: Double = 1.0
    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
    override var damageAmount: Float = 0f
    var style: TaiChiComposition? = null
    var shrink: ShrinkParticleEmitters? = null
    override fun onActive(source: MagicBookEntity) {
        damageAmount = 0f
        style = TaiChiComposition(source.position(), source.level())
        shrink = ShrinkParticleEmitters(source.position(), source.level())
            .apply {
                this.templateData.also {
                    it.effect = ControlableFireworkEffect(it.uuid)
                    it.maxAge = 20
                }
                maxTick = -1
            }
        ParticleCompositionManager.spawn(style!!)
        ParticleEmittersManager.spawnEmitters(shrink!!)
    }

    override fun onRelease(source: MagicBookEntity, holdingTick: Int) {
        val maxHealth = source.getUnlimitMaxHealth()
        source.health = maxHealth - source.health
        style?.status?.setStatus(2)
        shrink?.canceled = true
        style = null
        shrink = null
    }

    override fun getMaxHoldingTick(holdingEntity: MagicBookEntity): Int {
        return 20 * 30
    }

    override fun holdingTick(holdingEntity: MagicBookEntity, holdTicks: Int) {
        style?.teleportTo(holdingEntity.position())
        shrink?.pos = holdingEntity.position()
    }

    override fun getSkillCountDown(source: MagicBookEntity): Int = 20 * 60

    override fun stopHolding(entity: MagicBookEntity, holdTicks: Int) {
        entity.health = (entity.health - 50f).coerceAtLeast(0.5f)
        style?.status?.setStatus(2)
        style = null
        shrink?.canceled = true
        shrink = null
    }

    override fun getSkillID(): String {
        return "health-reverse"
    }

    override fun canTrigger(entity: MagicBookEntity): Boolean {
        val maxHealth = entity.getUnlimitMaxHealth()
        return entity.health <= maxHealth * 0.25
    }


    override fun maxDamage(entity: MagicBookEntity): Float {
        val maxHealth = entity.getUnlimitMaxHealth()
        return maxHealth * 0.07f
    }


}
