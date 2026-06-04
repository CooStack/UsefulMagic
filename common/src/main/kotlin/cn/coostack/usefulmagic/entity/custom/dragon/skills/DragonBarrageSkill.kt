package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.barrages.entity.skill.TrackedPointBarrage
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonOrbitCloserFlightPhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class DragonBarrageSkill : DragonSkill() {
    companion object {
        const val ID = "dragon-barage-skill"
    }

    override var chance: Double = 0.4
    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 38
    }

    override fun onActive(source: MagicDragonEntity) {
        source.phaseManager.forceSetPhase(
            DragonOrbitCloserFlightPhase()
        ) {
            val target = source.target ?: return@forceSetPhase
            val pos = target.boxCenterPosition()
            setCurrentTarget(pos)
            listenContinue {
                val target = source.target ?: return@listenContinue
                val pos = target.boxCenterPosition()
                setCurrentTarget(pos)
            }
            this.params.apply {
                this[DragonOrbitCloserFlightPhase.MIN_RADIUS] = 3.0
                this[DragonOrbitCloserFlightPhase.END_RADIUS] = 8.0
                this[DragonOrbitCloserFlightPhase.START_RADIUS] = 32.0
                this[DragonOrbitCloserFlightPhase.ORBIT_HEIGHT_LERP] = 0.2
            }
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 5
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        val currentTarget = holdingEntity.target ?: return

        holdingEntity.phaseManager.setCurrentTarget(currentTarget.boxCenterPosition())
        holdingEntity.lookAt(currentTarget, 20f, 20f)
        val world = holdingEntity.level() as ServerLevel
        val loc = holdingEntity.boxCenterPosition()
        world.playSound(
            null,
            loc.x,
            loc.y,
            loc.z,
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundSource.HOSTILE,
            10f,
            2f
        )
        if (holdTicks % 3 != 0) {
            return
        }

        // 找范围内的玩家
        val box = holdingEntity.boundingBox.inflate(64.0)
        val players = world.getEntitiesOfClass(Player::class.java, box) {
            it.isAlive && !(it.isSpectator || it.isCreative)
        }
        repeat(Random.nextInt(3, 6)) {
            val selectPlayer = if (players.isNotEmpty()) players.random() else currentTarget
            val backward = -holdingEntity.forward.normalize()
            val direction = (backward * 2.0 + backward.offsetRandomly(1.0)).normalize()
            val barrage = TrackedPointBarrage(selectPlayer, loc, world, 5.0, holdingEntity)
            barrage.direction = direction
            barrage.options.enableSpeedWithOptions(Random.nextDouble(1.8, 2.5))
            BarrageManager.spawn(barrage)
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String = ID
}