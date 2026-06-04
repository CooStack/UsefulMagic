package cn.coostack.usefulmagic.entity.custom.book.skills

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import kotlin.random.Random

class LightningShootSkill : Skill<MagicBookEntity> {
    override var chance: Double = 1.0


    override fun onActive(source: MagicBookEntity) {
    }

    override fun onRelease(source: MagicBookEntity, holdingTick: Int) {
        // target 收到伤害
        val target = source.target ?: let {
            handleMultipleDamage(source)
            return
        }

        handleSingleDamage(source, target)
    }

    private fun handleMultipleDamage(source: LivingEntity) {
        val world = source.level()
        world.explode(
            source,
            source.x, source.y, source.z, 10f, false, Level.ExplosionInteraction.MOB
        )
    }

    private val random = Random(System.currentTimeMillis())
    private fun handleSingleDamage(source: LivingEntity, target: LivingEntity) {
        val dir = source.eyePosition.relativize(target.eyePosition).normalize().scale(4.5)
        val lightning = LightningParticleEmitters(
            source.eyePosition.add(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
            ), source.level()
        )
            .apply {
//                endPos = RelativeLocation.of(source.eyePosition.relativize(target.eyePosition.add(dir)))
                maxTick = random.nextInt(1, 6)
                templateData.also {
                    it.maxAge = 5
                    it.color = Math3DUtil.colorOf(
                        121, 211, 249
                    )
                }
            }
        ParticleEmittersManager.spawnEmitters(lightning)
        val damageSource = source.damageSources().mobAttack(source)
        target.hurt(damageSource, 20f)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicBookEntity): Int {
        return 60
    }

    override fun getSkillCountDown(source: MagicBookEntity): Int {
        return 120
    }

    override fun holdingTick(holdingEntity: MagicBookEntity, holdTicks: Int) {
        val emitters = LightningParticleEmitters(holdingEntity.position(), holdingEntity.level()).apply {
//            endPos = RelativeLocation(
//                random.nextDouble(-20.0, 20.0),
//                random.nextDouble(0.0, 20.0),
//                random.nextDouble(-20.0, 20.0),
//            )
            maxTick = random.nextInt(1, 2)
        }

        ParticleEmittersManager.spawnEmitters(emitters)

    }

    override fun stopHolding(entity: MagicBookEntity, holdTicks: Int) {
        val explode = ExplodeMagicEmitters(entity.position(), entity.level())
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
