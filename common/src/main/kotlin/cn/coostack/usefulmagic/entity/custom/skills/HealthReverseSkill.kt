package cn.coostack.usefulmagic.entity.custom.skills

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.UnlimitHealthEntity
import cn.coostack.usefulmagic.particles.emitters.ShrinkParticleEmitters
import cn.coostack.usefulmagic.particles.style.skill.TaiChiStyle
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCancelable
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.skill.api.SkillDamageCancelCondition
import net.minecraft.world.entity.LivingEntity

/**
 * 技能设定和动画
 * 技能触发条件 生命值低于50% (单个血条)
 * 触发时间 20 * 30 ticks (30秒)
 * 触发动画 产生太极形状的粒子 进行旋转
 * 触发效果: 实体生命值 逆转  (health = maxHealth - health)
 * 中断技能
 *     触发技能时 受到了 maxHealth * 0.3 的伤害
 * 中断效果
 *     实体额外受到 50 伤害
 *     并且拥有 60秒的冷却 (20 * 60)
 *
 */
class HealthReverseSkill : Skill, SkillCondition, SkillDamageCancelCondition {
    override var chance: Double = 1.0
    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
    override var damageAmount: Float = 0f
    var style: TaiChiStyle? = null
    var shrink: ShrinkParticleEmitters? = null
    override fun onActive(source: LivingEntity) {
        damageAmount = 0f
        style = TaiChiStyle()
        shrink = ShrinkParticleEmitters(source.position(), source.level())
            .apply {
                this.templateData.also {
                    it.effect = ControlableFireworkEffect(it.uuid)
                    it.maxAge = 20
                }
                maxTick = -1
            }
        ParticleStyleManager.spawnStyle(source.level(), source.position(), style!!)
        ParticleEmittersManager.spawnEmitters(shrink!!)
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        val maxHealth = if (source is UnlimitHealthEntity) {
            source.getUnlimitMaxHealth()
        } else source.maxHealth
        source.health = maxHealth - source.health
        style?.status?.setStatus(2)
        shrink?.cancelled = true
        style = null
        shrink = null
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 20 * 30
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
        style?.teleportTo(holdingEntity.position())
        shrink?.pos = holdingEntity.position()
    }

    override fun getSkillCountDown(source: LivingEntity): Int = 20 * 60

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        entity.health = (entity.health - 50f).coerceAtLeast(0.5f)
        style?.status?.setStatus(2)
        style = null
        shrink?.cancelled = true
        shrink = null
    }

    override fun getSkillID(): String {
        return "health-reverse"
    }

    override fun canTrigger(entity: LivingEntity): Boolean {
        val maxHealth = if (entity is UnlimitHealthEntity) {
            entity.getUnlimitMaxHealth()
        } else entity.maxHealth
        return entity.health <= maxHealth * 0.25
    }


    override fun maxDamage(entity: LivingEntity): Float {
        val maxHealth = if (entity is UnlimitHealthEntity) {
            entity.getUnlimitMaxHealth()
        } else entity.maxHealth
        return maxHealth * 0.07f
    }


}