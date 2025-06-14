package cn.coostack.usefulmagic.entity.custom.skills

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.UnlimitHealthEntity
import cn.coostack.usefulmagic.particles.emitters.LineEmitters
import cn.coostack.usefulmagic.particles.emitters.ShrinkParticleEmitters
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 拔刀斩
 * 不断向实体发起攻击
 * 攻击会滞后10tick
 * 范围为 Box.of(center,4,4,4)
 */
class BookSwordSlashSkill(val damage: Float) : Skill, SkillCondition {
    override var chance: Double = 0.5
    private var holdingEmitters: ShrinkParticleEmitters? = null
    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        if (holdingEntity !is MagicBookEntity) return 5 * 20
        return when (holdingEntity.getHealthState()) {
            3 -> 5 * 20
            2 -> 8 * 20
            1 -> 10 * 20
            else -> 5 * 20
        }
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        if (source !is MagicBookEntity) return 25 * 20
        return when (source.getHealthState()) {
            3 -> 25 * 20
            2 -> 20 * 20
            1 -> 10 * 20
            else -> 25 * 20
        }
    }

    override fun onActive(source: LivingEntity) {
        holdingEmitters = ShrinkParticleEmitters(source.eyePos, source.world)
            .apply {
                this.templateData.also {
                    it.effect = ControlableFireworkEffect(it.uuid)
                    it.maxAge = 10
                    it.color = Math3DUtil.colorOf(255, 150, 80)
                }
                maxTick = -1
                speedDrag = 0.98
                startSpeed = 3.0
                startRange = 30.0
            }
        ParticleEmittersManager.spawnEmitters(holdingEmitters!!)
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        if (source is MagicBookEntity) {
            source.isAttacking = false
            source.attackTick = 0
        }
        holdingEmitters?.remove()
        holdingEmitters = null
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
        if (holdingEntity is MagicBookEntity) {
            holdingEntity.isAttacking = true
        }
        if (holdTicks % 4 == 0) {
            repeat(6) {
                val x = random.nextDouble(-10.0, 10.0)
                val y = random.nextDouble(-5.0, 10.0)
                val z = random.nextDouble(-10.0, 10.0)
                val pos = holdingEntity.pos.add(x, y, z)
                val line = RelativeLocation(0.0, random.nextDouble(1.0, 5.0), 0.0)
                val count = (line.length() * 2).roundToInt()
                val style = EnchantLineStyle(line, count, random.nextInt(30, 45))
                style.apply {
                    particleRandomAgePreTick = true
                    fade = true
                    this.colorOf(255, 150, 80)
                    fadeInTick = 15
                    fadeOutTick = 15
                    speedDirection = RelativeLocation(0.0, random.nextDouble(-0.1, 0.1), 0.0)
                }
                ParticleStyleManager.spawnStyle(holdingEntity.world!!, pos, style)
            }
        }
        holdingEmitters?.teleportTo(holdingEntity.pos)
        val interval = if (holdingEntity is MagicBookEntity) {
            when (holdingEntity.getHealthState()) {
                3 -> 6
                2 -> 3
                1 -> 2
                else -> 6
            }
        } else 6
        if (holdTicks % interval == 0) {
            handleOnceDamage(holdingEntity)
        }
    }

    val random = Random(System.currentTimeMillis())
    private fun handleOnceDamage(attacker: LivingEntity) {
        if (attacker !is MobEntity) return
        val target = attacker.target ?: return
        val targetPos = target.eyePos.add(0.0, -0.5, 0.0)
        val spawnPos = targetPos.add(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        val dir = spawnPos.relativize(targetPos)
        CooParticleAPI.scheduler.runTask(10) {
            val emitter = LineEmitters(spawnPos, attacker.world).apply {
                templateData.apply {
                    effect = ControlableCloudEffect(uuid)
                    color = Math3DUtil.colorOf(255, 150, 80)
                    maxAge = 20
                }
                maxTick = 1
                this.endPos = dir.multiply(2.0)
                this.count = (dir.length() * 3).toInt()
            }
            val world = attacker.world
            world.playSound(
                null, attacker.x, attacker.y, attacker.z,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 10f, 2f
            )
            ParticleEmittersManager.spawnEmitters(emitter)
            world.getEntitiesByClass(LivingEntity::class.java, Box.of(targetPos, 4.0, 4.0, 4.0)) {
                it.uuid != attacker.uuid
            }.forEach {
                val source = it.damageSources.mobAttack(attacker)
                it.damage(source, damage)
                it.timeUntilRegen = 0
                it.hurtTime = 0
            }
        }
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        if (entity is MagicBookEntity) {
            entity.isAttacking = false
            entity.attackTick = 0
        }
        holdingEmitters?.remove()
        holdingEmitters = null
    }

    override fun getSkillID(): String {
        return "book-sword-slash-skill"
    }


    override fun canTrigger(entity: LivingEntity): Boolean {
        if (entity !is UnlimitHealthEntity) return false
        if (entity !is MobEntity) return false
        return entity.target != null
    }
}