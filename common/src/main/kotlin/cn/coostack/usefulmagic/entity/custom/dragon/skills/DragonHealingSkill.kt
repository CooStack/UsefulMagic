package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicHeartEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonHoverFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.composition.MagicRuneRingComposition
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.random.Random

/**
 *
 * TODO 当实体被召唤的时候， 需要有一个粒子魔法阵 并且对他不断进行恢复
 *
 * 召唤5个实体在周围， 然后龙会停留不动（无敌时间）
 *
 * 攻击这5个实体， 20秒之后， 龙会恢复血量 （存活的实体血量之和）
 *
 * @constructor Create empty Dragon breath skill
 */
class DragonHealingSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_breath_skill"

        val positions = PointsBuilder()
            .addCircle(32.0, 5)
            .createWithoutClone()

    }

    override var chance: Double = 0.3

    private val entities = arrayListOf<MagicHeartEntity>()
    private var index = 0

    private val compositions = ArrayList<ParticleComposition>()

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 96 * 20
    }


    override fun onActive(source: MagicDragonEntity) {
        entities.clear()
        compositions.clear()
        index = 0
        source.phaseManager.trySetPhase(
            DragonHoverFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition)
            listenContinue {
                setCurrentTarget(source.spawnPosition)
            }
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        // 释放回血
        // 清理所有的heart 然后转换为一个health的能量
        entities.filter {
            it.isAlive
        }.forEach {
            source.health = (source.health + it.health * 2).coerceAtMost(source.dragonMaxHealth)
            LightningParticleEmitters(it.boxCenterPosition(), it.level()).apply {
                targetPos = source.boxCenterPosition() - it.boxCenterPosition()
                templateData.apply {
                    color = Math3DUtil.colorOf(120, 255, 50)
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
                maxTick = 1
                spawn(world!!, pos)
            }
            it.kill()
        }
        reset(source)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 40
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        if (holdTicks <= 20 * 5) {
            return
        }


        if (holdTicks == 101) {
            // 两个 然后换不同的axis进行旋转
            compositions.apply {
                add(MagicRuneRingComposition(holdingEntity.boxCenterPosition(), holdingEntity.level()).apply {
                    radianAlphaOffset = -PI / 8
                    radianThetaOffset = PI * 5 / 4
                    thetaRotationSpeed = PI / 64
                    alphaRotationSpeed = PI / 48
                    radius = 12.0
                    ParticleCompositionManager.spawn(this)
                })
                add(
                    MagicRuneRingComposition(holdingEntity.boxCenterPosition(), holdingEntity.level())
                        .apply {
                            radius = 8.0
                            radianAlphaOffset = PI / 4
                            radianThetaOffset = PI / 2
                            thetaRotationSpeed = PI / 48
                            alphaRotationSpeed = PI / 64
                            ParticleCompositionManager.spawn(this)
                        }
                )
                add(
                    MagicRuneRingComposition(holdingEntity.boxCenterPosition(), holdingEntity.level())
                        .apply {
                            radius = 10.0
                            radianAlphaOffset = PI * 1.5
                            radianThetaOffset = PI
                            thetaRotationSpeed = PI / 64
                            alphaRotationSpeed = PI / 72
                            ParticleCompositionManager.spawn(this)
                        }
                )
            }

        }

        // 100tick内， 每20个tick生成一个heart
        if (holdTicks in 20 * 5..20 * 10) {
            if (holdTicks % 20 != 0 || index == 5) {
                return
            }
            // 这里要找5个位置进行设置
            val rel = positions[index++].toVector()
            val targetSpawnPos = holdingEntity.spawnPosition + rel

            // 生成实体
            val entity = MagicHeartEntity(holdingEntity.level())
                .apply {
                    setPos(targetSpawnPos)
                    owner = holdingEntity
                    level()
                        .addFreshEntity(this)
                }

            playDragonSoundOnce(
                entity,
                SoundEvents.CONDUIT_ACTIVATE,
                SoundSource.HOSTILE,
                1f,
                Random.nextFloat() * 0.3f + 1f,
                128.0,
            )


            entities.add(entity)
            return
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        reset(entity)
    }

    fun reset(entity: MagicDragonEntity) {
        entity.phaseManager.resetDefaultPhase()
        compositions.forEach(ParticleComposition::remove)
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun testCancel(entity: MagicDragonEntity): Boolean {
        // 没有一个是活着的但是全部生成的情况下会提前取消
        return super.testCancel(entity) || (entities.size == 5 && !entities.any { it.isAlive })
    }

    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return super.canTrigger(entity) && entity.health < entity.dragonMaxHealth * 0.6
    }


}
