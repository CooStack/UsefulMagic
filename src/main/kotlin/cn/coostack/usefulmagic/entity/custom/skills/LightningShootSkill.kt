package cn.coostack.usefulmagic.entity.custom.skills

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.world.World
import kotlin.random.Random

class LightningShootSkill : Skill {
    override var chance: Double = 1.0


    override fun onActive(source: LivingEntity) {
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        // target 收到伤害
        if (source !is MobEntity) {
            // 群体伤害
            handleMultipleDamage(source)
            return
        }

        val target = source.target ?: let {
            handleMultipleDamage(source)
            return
        }

        handleSingleDamage(source, target)
    }

    private fun handleMultipleDamage(source: LivingEntity) {
        val world = source.world
        world.createExplosion(
            source,
            source.x, source.y, source.z, 10f, false, World.ExplosionSourceType.MOB
        )
    }

    private val random = Random(System.currentTimeMillis())
    private fun handleSingleDamage(source: LivingEntity, target: LivingEntity) {
        val dir = source.eyePos.relativize(target.eyePos).normalize().multiply(4.5)
        val lightning = LightningParticleEmitters(
            source.eyePos.add(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
            ), source.world
        )
            .apply {
                endPos = RelativeLocation.of(source.eyePos.relativize(target.eyePos.add(dir)))
                maxTick = random.nextInt(1, 6)
                templateData.also {
                    it.maxAge = 5
                    it.color = Math3DUtil.colorOf(
                        121, 211, 249
                    )
                }
            }
        ParticleEmittersManager.spawnEmitters(lightning)
        val damageSource = source.damageSources.mobAttack(source)
        target.damage(damageSource, 20f)
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 60
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        return 120
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
        val emitters = LightningParticleEmitters(holdingEntity.pos, holdingEntity.world).apply {
            endPos = RelativeLocation(
                random.nextDouble(-20.0, 20.0),
                random.nextDouble(0.0, 20.0),
                random.nextDouble(-20.0, 20.0),
            )
            maxTick = random.nextInt(1, 2)
        }

        ParticleEmittersManager.spawnEmitters(emitters)

    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        val explode = ExplodeMagicEmitters(entity.pos, entity.world)
            .apply {
                maxTick = 1
                minSpeed = 1.0
                maxSpeed = 5.0
                ballCountPow = 20
                randomCountMin = 100
                randomCountMax = 200
                precentDrag = 0.9
                randomParticleAgeMin = 30
                randomParticleAgeMax = 80
            }
        ParticleEmittersManager.spawnEmitters(explode)
    }

    override fun getSkillID(): String {
        return "lightning-shoot"
    }
}