package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonOrbitCloserFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.composition.MagicConjureEyeComposition
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.emitters.entity.eye.MagicEyeHurtEmitter
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 一口气召唤3个 召唤10次
 *
 * 其中 2个是半血（自杀式） 1个是满血
 *
 * 此技能释放时， 龙悬浮在spawnPosition上
 */
class DragonConjureSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_split_skill"
    }

    private var composition: MagicConjureEyeComposition? = null
    override var chance: Double = 0.3

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 45
    }


    override fun onActive(source: MagicDragonEntity) {
        source.phaseManager.forceSetPhase(
            DragonOrbitCloserFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition)
            params.apply {
                this[DragonOrbitCloserFlightPhase.MIN_RADIUS] = 1.0
                this[DragonOrbitCloserFlightPhase.START_RADIUS] = 64.0
                this[DragonOrbitCloserFlightPhase.END_RADIUS] = 0.0
                this[DragonOrbitCloserFlightPhase.WARMUP_TICKS] = 10
            }
        }
        composition = MagicConjureEyeComposition(source.boxCenterPosition().add(0.0, -1.0, 0.0), source.level()).apply {
            ParticleCompositionManager.spawn(this)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        clear(source)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 10
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        composition?.teleportTo(holdingEntity.boxCenterPosition().add(0.0, -1.0, 0.0))
        if (holdTicks <= 20 * 3) {
            return
        }

        val step = 20
        val shouldSpawned = holdTicks % step == 0
        if (!shouldSpawned) {
            return
        }

        // conjure
        val world = holdingEntity.level()

        val positions = arrayListOf<Vec3>()

        // 1个满血 2个半血 在龙的周围生成
        val fullOne = MagicSubEyeEntity(world)
        fullOne.owner = holdingEntity
        val fullPos = holdingEntity.boxCenterPosition().offsetRandomly(
            Random.nextDouble(16.0, 32.0)
        ).withY {
            if (y <= holdingEntity.y) holdingEntity.y + Random.nextDouble(16.0, 32.0) else y
        }
        MagicEyeHurtEmitter(fullPos, world).apply {
            ParticleEmittersManager.spawnEmitters(this)
        }
        positions.add(fullPos)
        fullOne.setPos(fullPos)
        world.addFreshEntity(fullOne)
        // 位置如何生成？
        // 1. 使用龙周围的位置
        // 2. 出生位置随机
        repeat(2) {
            val subOne = MagicSubEyeEntity(world)
            subOne.owner = holdingEntity
            val subPos = holdingEntity.boxCenterPosition().offsetRandomly(
                Random.nextDouble(16.0, 32.0)
            ).withY {
                if (y <= holdingEntity.y) holdingEntity.y + Random.nextDouble(16.0, 32.0) else y
            }
            subOne.setPos(subPos)
            subOne.health = subOne.maxHealth / 2 - 1f
            world.addFreshEntity(subOne)
            MagicEyeHurtEmitter(subPos, world).apply {
                ParticleEmittersManager.spawnEmitters(this)
            }
            positions.add(subPos)
        }
        positions.forEach {
            LightningParticleEmitters(holdingEntity.boxCenterPosition(), holdingEntity.level()).apply {
                targetPos = it - pos
                templateData.apply {
                    color = Math3DUtil.colorOf(255, 100, 200)
                    setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                }
                simpleData.apply {
                    minCount = 3
                    maxCount = 6
                    minAge = 3
                    maxAge = 7
                    minSize = 0.1
                    maxSize = 0.3
                }
                spawn(world, pos)
                maxTick = 1
            }
        }
        // hurt(粒子释放）

        playDragonSoundOnce(
            holdingEntity,
            UsefulMagicSoundEvents.DRAGON_MAGIC_ACTIVE.get(),
            SoundSource.HOSTILE,
            1f,
            1f,
            256.0,
        )

    }


    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clear(entity)
    }


    private fun clear(entity: MagicDragonEntity) {
        entity.phaseManager.resetDefaultPhase()
        composition?.remove()
    }

    override fun getSkillID(): String {
        return ID
    }
}
