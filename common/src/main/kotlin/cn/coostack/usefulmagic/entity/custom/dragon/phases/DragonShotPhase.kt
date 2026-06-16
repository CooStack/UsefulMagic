package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.serverLevelApply
import cn.coostack.usefulmagic.renderer.ShotWaveBillboardRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

class DragonShotPhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "dragon_shot"
    }

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return !instance.dragonSpawnShot
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        val spawn = instance.spawnPosition
        runtime.addTarget(spawn)
        // 这里要锁死在出生点
        // 设置姿态
        instance.teleportTo(spawn.x, spawn.y, spawn.z)
        instance.playAnimation(
            MagicDragonAnimationState.BURST_UP_OPEN_WINGS
        )

    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val tick = runtime.phaseAge
        if (tick >= MagicDragonAnimationState.BURST_UP_OPEN_WINGS.durationTicks) {
            return PhaseResult.Next(DragonSimpleFlightPhase())
        }

        // tick>1秒
        if (tick < 17) {
            return PhaseResult.Continue
        }
        if (tick == 17) {
            instance.serverLevelApply {
                playDragonSoundOnce(
                    it,
                    spawnPosition,
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE,
                    10f,
                    1f,
                    512.0
                )
                CooPostEffects.server.spawn(it, UsefulMagicPostEffects.rgbDashBlur(20, 2f, 2f))
            }
        }
        if (tick % 4 == 0) {
            instance.serverLevelApply {
                // 咆哮spawn
                ShotWaveBillboardRenderEntity.spawn(
                    it,
                    this.spawnPosition.add(0.0, 15.0, 0.0),
                    fadeInTick = 10,
                    fadeOutTick = 15,
                    initialScale = 0.1f,
                    limitScale = 20f,
                    scaleSpeed = 20f,
                    rollSpeed = -.25f,
                    useWave2Texture = false,
                    alpha = 0.5
                )
            }
        }
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        instance.dragonSpawnShot = true
    }

    override fun id(): String {
        return ID
    }
}
