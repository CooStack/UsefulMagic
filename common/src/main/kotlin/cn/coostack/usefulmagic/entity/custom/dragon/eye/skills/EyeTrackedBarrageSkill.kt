package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.barrages.entity.skill.TrackedPointBarrage
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeKeepDistancePhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class EyeTrackedBarrageSkill(val searchRange: Double, val damage: Double) : Skill<MagicEyeEntity>,
    SkillCancelCondition<MagicEyeEntity> {
    companion object {
        const val ID = "eye_tracked_barrage_skill"
    }

    override var chance: Double = 0.4

    override fun getSkillCountDown(source: MagicEyeEntity): Int {
        return 20 * 20
    }

    override fun onActive(source: MagicEyeEntity) {
        source.phaseManager.forceSetPhase(
            EyeKeepDistancePhase()
        ) {
            listenContinue {
                val target = source.target?.takeIf { source.canAttackWithEyeMagic(it) } ?: return@listenContinue
                setCurrentTarget(target.boxCenterPosition())
            }
            val target = source.target?.takeIf { source.canAttackWithEyeMagic(it) } ?: return@forceSetPhase
            setCurrentTarget(target.boxCenterPosition())
            params.apply {
                this[EyeKeepDistancePhase.MIN_CATCH_UP] = 30.0
                this[EyeKeepDistancePhase.MAX_CATCH_UP] = 32.0
                this[EyeKeepDistancePhase.DAMPING] = 0.97
                this[EyeKeepDistancePhase.ACCELERATION] = 1.2
                this[EyeKeepDistancePhase.HIGHER_THAN_TARGET] = true
                this[EyeKeepDistancePhase.HEIGHT_RELATIVE_LEAST] = 3.0
                this[EyeKeepDistancePhase.HEIGHT_RELATIVE_MOST] = 6.0
            }
        }
    }

    override fun onRelease(source: MagicEyeEntity, holdingTick: Int) {
        source.phaseManager.resetDefaultPhase()
    }

    override fun getMaxHoldingTick(holdingEntity: MagicEyeEntity): Int {
        return 20 * 3
    }

    override fun holdingTick(
        holdingEntity: MagicEyeEntity,
        holdTicks: Int
    ) {
        val currentTarget = holdingEntity.target?.takeIf { holdingEntity.canAttackWithEyeMagic(it) } ?: run {
            holdingEntity.target = null
            return
        }
        holdingEntity.phaseManager.setCurrentTarget(currentTarget.boxCenterPosition())
        holdingEntity.lookAt(currentTarget, 20f, 20f)
        val world = holdingEntity.level() as ServerLevel
        val loc = holdingEntity.positionOnEye()
        world.playSound(
            null,
            loc.x,
            loc.y,
            loc.z,
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundSource.HOSTILE,
            3f,
            2f
        )
        if (holdTicks % 3 != 0) {
            return
        }

        // 找范围内的玩家
        val box = holdingEntity.boundingBox.inflate(searchRange)
        val players = world.getEntitiesOfClass(Player::class.java, box) {
            it.isAlive && !(it.isSpectator || it.isCreative)
        }
        repeat(Random.nextInt(3, 6)) {
            val selectPlayer = if (players.isNotEmpty()) players.random() else currentTarget
            val backward = -holdingEntity.forward.normalize()
            val direction = (backward * 2.0 + backward.offsetRandomly(1.0)).normalize()
            val barrage = TrackedPointBarrage(selectPlayer, loc, world, damage, holdingEntity)
            barrage.direction = direction
            barrage.options.speed(1.7)
            BarrageManager.spawn(barrage)
        }

    }

    override fun stopHolding(entity: MagicEyeEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun testCancel(entity: MagicEyeEntity): Boolean {
        return entity.health <= 1f
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
}
