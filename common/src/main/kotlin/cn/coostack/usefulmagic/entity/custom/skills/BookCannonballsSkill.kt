package cn.coostack.usefulmagic.entity.custom.skills

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DiscreteCylinderEmitters
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.emitters.ShrinkParticleEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.skill.api.SkillDamageCancelCondition
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

class BookCannonballsSkill(val damage: Float) : Skill, SkillCondition, SkillDamageCancelCondition {
    override var chance: Double = 2.0
    override var damageAmount: Float = 0f

    private var holdingEmitters: ShrinkParticleEmitters? = null
    var activePos: Vec3 = Vec3.ZERO
    override fun onActive(source: LivingEntity) {
        tick = 0
        damageAmount = 0f
        holdingEmitters = ShrinkParticleEmitters(source.eyePosition, source.level())
            .apply {
                this.templateData.also {
                    it.effect = ControlableFireworkEffect(it.uuid)
                    it.maxAge = 10
                    it.color = Math3DUtil.colorOf(150, 200, 255)
                }
                maxTick = -1
                speedDrag = 0.98
                startSpeed = 1.0
                startRange = 10.0
            }
        activePos = source.position()
        ParticleEmittersManager.spawnEmitters(holdingEmitters!!)
        canceled = false
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        if (source !is MagicBookEntity) return 15 * 20
        return when (source.getHealthState()) {
            3 -> 15 * 20
            2 -> 10 * 20
            1 -> 5 * 20
            else -> 15 * 20
        }
    }

    override fun onRelease(sourceEntity: LivingEntity, holdingTick: Int) {
        if (sourceEntity !is Mob) {
            return
        }
        if (sourceEntity is MagicBookEntity) {
            sourceEntity.setAttacking(false)
            sourceEntity.attackTick = 0
        }
        ServerCameraUtil.sendShake(sourceEntity.level() as ServerLevel, sourceEntity.position(), 64.0, 0.5, 10)
        sourceEntity.fallDistance = 0f
        // 暂时没写不带target参数的
        val target = sourceEntity.target ?: return
        val direction = sourceEntity.position().relativize(target.position())
        val discrete = DiscreteCylinderEmitters(sourceEntity.eyePosition, sourceEntity.level())
            .apply {
                this.direction = direction
                maxTick = 1
                minCount = 5
                maxCount = 10
                height = target.distanceTo(sourceEntity).toDouble() * 1.5
                minDiscrete = 0.5
                maxDiscrete = 1.0
                radiusStep = 0.4
                maxRadius = 2.0
                heightStep = 1.0
                templateData.also {
                    it.effect = ControlableCloudEffect(it.uuid)
                    it.maxAge = 40
                }
            }
        ParticleEmittersManager.spawnEmitters(discrete)
        val normalizer = direction.normalize()
        sourceEntity.deltaMovement = direction.scale(-0.25)
        sourceEntity.hurtMarked = true
        val r1 = ParticleWaveEmitters(sourceEntity.eyePosition.add(normalizer.scale(2.0)), sourceEntity.level())
            .apply {
                this.waveAxis = normalizer
                this.waveSpeed = 0.0
                this.waveSize = 1.5
                maxTick = 1
                this.templateData.also {
                    it.maxAge = 20
                    it.color = Math3DUtil.colorOf(100, 100, 255)
                }
            }

        val r2 = ParticleWaveEmitters(sourceEntity.eyePosition.add(normalizer.scale(4.0)), sourceEntity.level())
            .apply {
                this.waveAxis = normalizer
                this.waveSpeed = 0.0
                this.waveSize = 3.0
                maxTick = 1
                this.templateData.also {
                    it.maxAge = 20
                    it.color = Math3DUtil.colorOf(100, 100, 255)
                }
            }

        val r3 = ParticleWaveEmitters(sourceEntity.eyePosition.add(normalizer.scale(8.0)), sourceEntity.level())
            .apply {
                this.waveAxis = normalizer
                this.waveSpeed = 0.0
                this.waveSize = 2.0
                maxTick = 1
                this.templateData.also {
                    it.maxAge = 20
                    it.color = Math3DUtil.colorOf(100, 100, 255)
                }
            }
        ParticleEmittersManager.spawnEmitters(r1)
        ParticleEmittersManager.spawnEmitters(r2)
        ParticleEmittersManager.spawnEmitters(r3)
        val world = sourceEntity.level()
        world.playSound(
            null,
            sourceEntity.x,
            sourceEntity.y,
            sourceEntity.z,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.MASTER,
            5f,
            1.5f
        )
        val source = target.damageSources().mobAttack(sourceEntity)
        target.hurt(source, damage)
        val vd = direction.scale(0.15)
        target.deltaMovement = Vec3(vd.x, abs(vd.y) * 0.5 + 0.05, vd.z)
        target.hurtMarked = true
        holdingEmitters?.cancelled = true
        holdingEmitters = null
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        if (holdingEntity !is MagicBookEntity) return 4 * 20
        return when (holdingEntity.getHealthState()) {
            3 -> 4 * 20
            2 -> 2 * 20
            1 -> 1 * 20
            else -> 4 * 20
        }
    }

    private var tick = 0
    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
        if (holdingEntity is MagicBookEntity) {
            holdingEntity.setAttacking(true)
        }
        if (holdingEntity.y < activePos.y + 6.0) {
            holdingEntity.deltaMovement = holdingEntity.deltaMovement.add(0.0, 0.06, 0.0)
        } else if (holdingEntity.y > activePos.y + 8.0) {
            holdingEntity.deltaMovement = holdingEntity.deltaMovement.add(0.0, -0.06, 0.0)
        }
        holdingEmitters?.pos = holdingEntity.eyePosition
        if (tick++ % 5 == 0) {
            holdingEntity.level().playSound(
                null,
                holdingEntity.x,
                holdingEntity.y,
                holdingEntity.z,
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.HOSTILE,
                5f, 0.8f
            )
        }
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        holdingEmitters?.cancelled = true
        holdingEmitters = null
        entity.level().playSound(
            null, entity.x, entity.y, entity.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 2f
        )
        if (canceled) {
            val attacker = entity.lastAttacker
            if (attacker != null) {
                entity.hurt(
                    if (attacker is Player) {
                        entity.damageSources().playerAttack(attacker)
                    } else entity.damageSources().mobAttack(
                        attacker
                    ), damage * 2
                )
            } else {
                entity.hurt(entity.damageSources().generic(), damage * 2)
            }
        }

        if (entity is MagicBookEntity) {
            entity.setAttacking(false)
            entity.attackTick = 0
        }

        val explosion = ExplodeMagicEmitters(entity.position(), entity.level()).apply {
            this.templateData.also {
                it.size = 0.4f
            }
            randomParticleAgeMin = 20
            randomParticleAgeMax = 60
            precentDrag = 0.95
            maxTick = 1
            ballCountPow = 10
            minSpeed = 0.5
            maxSpeed = 5.0
            randomCountMin = 30
            randomCountMax = 80
        }
        ParticleEmittersManager.spawnEmitters(explosion)

        tick = 0
    }

    override fun getSkillID(): String {
        return "book-cannonball-skill"
    }

    override fun canTrigger(entity: LivingEntity): Boolean {
        if (entity !is Mob) return true
        return entity.target != null
    }


    override fun maxDamage(entity: LivingEntity): Float {
        return 30f
    }

    override fun testCancel(entity: LivingEntity): Boolean {
        return !canTrigger(entity) || super.testCancel(entity)
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
}