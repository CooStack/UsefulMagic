package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeHoverPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.barrages.entity.skill.StraightPointBarrage
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.sounds.SoundSource
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class EyeTeleportingAttackSkill(val damage: Double) : Skill<MagicEyeEntity>, SkillCancelCondition<MagicEyeEntity> {
    companion object {
        const val ID = "eye_teleporting_attack"
    }

    override var chance: Double = 0.3
    private var offsetTick = 0
    private var waitTimeout = false
    private var firstTick = false
    override fun getSkillCountDown(source: MagicEyeEntity): Int {
        return 20 * 22
    }

    override fun onActive(source: MagicEyeEntity) {
        offsetTick = 0
        waitTimeout = false
        firstTick = false
        source.phaseManager.forceSetPhase(
            EyeHoverPhase()
        ) {
            setCurrentTarget(source.spawnPos.add(0.0, 15.0, 0.0))
            listenContinue {
                peekTarget().ifPresent {
                    if (it.distanceToSqr(source.positionOnEye()) <= 1) {
                        waitTimeout = true
                    }
                }
            }
        }
    }

    override fun onRelease(source: MagicEyeEntity, holdingTick: Int) {
    }

    override fun getMaxHoldingTick(holdingEntity: MagicEyeEntity): Int {
        return 20 * 6 + offsetTick
    }

    override fun holdingTick(
        holdingEntity: MagicEyeEntity,
        holdTicks: Int
    ) {
        if (!firstTick) {
            firstTick = true
            // 播放音效

            playDragonSoundOnce(
                holdingEntity,
                UsefulMagicSoundEvents.EYE_ACTIVE.get(),
                SoundSource.HOSTILE,
                1f,
                2f,
                256.0,
            )

        }
        if (offsetTick < 60 && !waitTimeout) {
            offsetTick++
            return
        } else {
            waitTimeout = true
        }

        // 这里要在每次传送的时候都要进行弹幕的生成
        if (holdTicks % 2 != 0) {
            return
        }

        playDragonSoundOnce(
            holdingEntity,
            UsefulMagicSoundEvents.EYE_TELEPORT.get(),
            SoundSource.HOSTILE,
            0.5f,
            1f,
            256.0,
        )

        val target = holdingEntity.target ?: return
        val direction = (target.boxCenterPosition() - holdingEntity.positionOnEye()).normalize()
        val barrage =
            StraightPointBarrage(holdingEntity.positionOnEye(), holdingEntity.serverLevel!!, damage, holdingEntity)
        barrage.direction = direction
        barrage.leftColor = Math3DUtil.colorOf(124, 118, 178)
        barrage.options.speed(3.0)
        BarrageManager.spawn(barrage)
        holdingEntity.teleportRandomly(Random.nextDouble(32.0, 64.0))
    }

    override fun stopHolding(entity: MagicEyeEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun testCancel(entity: MagicEyeEntity): Boolean {
        return entity.health <= 1f
    }

    override var canceled: Boolean = true
    override var cancelSetCD: Boolean = false
}
