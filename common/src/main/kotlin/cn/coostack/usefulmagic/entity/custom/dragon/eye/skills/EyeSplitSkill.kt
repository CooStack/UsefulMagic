package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills

import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeHoverPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.sounds.SoundSource
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 大概会分裂10个左右 血量30
 *
 * @constructor Create empty Eye split skill
 */
class EyeSplitSkill : Skill<MagicEyeEntity>, SkillCondition<MagicEyeEntity>, SkillCancelCondition<MagicEyeEntity> {
    companion object {
        const val ID = "eye_split_skill"
    }

    override var chance: Double = 0.0
    override fun testCancel(entity: MagicEyeEntity): Boolean {
        return entity.health <= 1f
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
    override fun getSkillCountDown(source: MagicEyeEntity): Int {
        return 20 * 28
    }

    override fun onActive(source: MagicEyeEntity) {
        source.phaseManager.forceSetPhase(
            EyeHoverPhase()
        ) {
            setCurrentTarget(source.spawnPos.add(0.0, 5.0, 0.0))
        }
    }

    override fun onRelease(source: MagicEyeEntity, holdingTick: Int) {
        source.phaseManager.resetDefaultPhase()
    }

    override fun getMaxHoldingTick(holdingEntity: MagicEyeEntity): Int {
        return 20 * 5
    }

    override fun holdingTick(
        holdingEntity: MagicEyeEntity,
        holdTicks: Int
    ) {
        if (holdTicks % 10 != 0) {
            return
        }
        // 释放之前 先制作延时
        // 相同位置释放一个Composition
        submitTaskServer(20) {
            holdingEntity.spawnSubEye(
                holdingEntity.positionOnEye()
                    .offsetRandomly(Random.nextDouble(3.0, 6.0))
            )

            playDragonSoundOnce(
                holdingEntity,
                UsefulMagicSoundEvents.MAGIC_ACTIVATE.get(),
                SoundSource.HOSTILE,
                1f,
                1 + Random.nextFloat(),
                256.0,
            )
        }
    }

    override fun stopHolding(entity: MagicEyeEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun canTrigger(entity: MagicEyeEntity): Boolean {
        return entity.countOwnedSubEyes() < MagicEyeEntity.MAX_SUB_EYE_COUNT
    }
}
