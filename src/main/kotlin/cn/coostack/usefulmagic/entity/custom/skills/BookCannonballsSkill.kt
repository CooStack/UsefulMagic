package cn.coostack.usefulmagic.entity.custom.skills

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
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import kotlin.math.abs

class BookCannonballsSkill(val damage: Float) : Skill, SkillCondition, SkillDamageCancelCondition {
    override var chance: Double = 2.0
    override var damageAmount: Float = 0f

    private var holdingEmitters: ShrinkParticleEmitters? = null
    var activePos: Vec3d = Vec3d.ZERO
    override fun onActive(source: LivingEntity) {
        tick = 0
        damageAmount = 0f
        holdingEmitters = ShrinkParticleEmitters(source.eyePos, source.world)
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
        activePos = source.pos
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
        if (sourceEntity !is MobEntity) {
            return
        }
        if (sourceEntity is MagicBookEntity) {
            sourceEntity.isAttacking = false
            sourceEntity.attackTick = 0
        }
        ServerCameraUtil.sendShake(sourceEntity.world as ServerWorld, sourceEntity.pos, 64.0, 0.5, 10)
        sourceEntity.fallDistance = 0f
        // 暂时没写不带target参数的
        val target = sourceEntity.target ?: return
        val direction = sourceEntity.pos.relativize(target.pos)
        val discrete = DiscreteCylinderEmitters(sourceEntity.eyePos, sourceEntity.world)
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
        sourceEntity.velocity = direction.multiply(-0.25)
        sourceEntity.velocityModified = true
        val r1 = ParticleWaveEmitters(sourceEntity.eyePos.add(normalizer.multiply(2.0)), sourceEntity.world)
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

        val r2 = ParticleWaveEmitters(sourceEntity.eyePos.add(normalizer.multiply(4.0)), sourceEntity.world)
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

        val r3 = ParticleWaveEmitters(sourceEntity.eyePos.add(normalizer.multiply(8.0)), sourceEntity.world)
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
        val world = sourceEntity.world
        world.playSound(
            null,
            sourceEntity.x,
            sourceEntity.y,
            sourceEntity.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.MASTER,
            5f,
            1.5f
        )
        val source = target.damageSources.mobAttack(sourceEntity)
        target.damage(source, damage)
        val vd = direction.multiply(0.15)
        target.velocity = Vec3d(vd.x, abs(vd.y) * 0.5 + 0.05, vd.z)
        target.velocityModified = true
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
            holdingEntity.isAttacking = true
        }
        if (holdingEntity.y < activePos.y + 6.0) {
            holdingEntity.velocity = holdingEntity.velocity.add(0.0, 0.06, 0.0)
        } else if (holdingEntity.y > activePos.y + 8.0) {
            holdingEntity.velocity = holdingEntity.velocity.add(0.0, -0.06, 0.0)
        }
        holdingEmitters?.pos = holdingEntity.eyePos
        if (tick++ % 5 == 0) {
            holdingEntity.world.playSound(
                null,
                holdingEntity.x,
                holdingEntity.y,
                holdingEntity.z,
                SoundEvents.ITEM_BOOK_PAGE_TURN,
                SoundCategory.HOSTILE,
                5f, 0.8f
            )
        }
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        holdingEmitters?.cancelled = true
        holdingEmitters = null
        entity.world.playSound(
            null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 5f, 2f
        )
        if (canceled) {
            val attacker = entity.attacker
            if (attacker != null) {
                entity.damage(
                    if (attacker is PlayerEntity) {
                        entity.damageSources.playerAttack(attacker as PlayerEntity)
                    } else entity.damageSources.mobAttack(
                        attacker
                    ), damage * 2
                )
            } else {
                entity.damage(entity.damageSources.generic(), damage * 2)
            }
        }

        if (entity is MagicBookEntity) {
            entity.isAttacking = false
            entity.attackTick = 0
        }

        val explosion = ExplodeMagicEmitters(entity.pos, entity.world).apply {
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
        if (entity !is MobEntity) return true
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