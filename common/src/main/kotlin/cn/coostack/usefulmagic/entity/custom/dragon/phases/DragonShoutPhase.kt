package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.cooparticlesapi.extend.plus
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
import net.minecraft.world.phys.Vec3

/** 控制魔龙出生时的咆哮、定点姿态和冲击波效果。 */
class DragonShoutPhase : PhaseDefinition<MagicDragonEntity> {
    /** 咆哮阶段的稳定标识。 */
    companion object {
        /** 阶段管理器使用的稳定 ID。 */
        const val ID = "dragon_shot"
    }

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return !instance.dragonSpawnShout
    }

    /**
     * 将魔龙锁定在出生点，并开始播放展开翅膀的咆哮动画。
     *
     * @param instance 当前魔龙实体
     * @param runtime 当前阶段运行状态
     */
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
        instance.animateTicking = MagicDragonAnimationState.BURST_UP_OPEN_WINGS.durationTicks

    }

    /**
     * 推进咆哮动画，并在指定时刻播放声音、后处理和冲击波。
     *
     * @param instance 当前魔龙实体
     * @param runtime 当前阶段运行状态
     * @return 本 tick 的阶段推进结果
     */
    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val tick = runtime.phaseAge
        if (tick >= MagicDragonAnimationState.BURST_UP_OPEN_WINGS.durationTicks) {
            instance.dragonSpawnShout = true
            instance.playAnimation(MagicDragonAnimationState.FLY)
            return PhaseResult.Next(DragonSimpleFlightPhase())
        }

        // 阶段经过一秒后再播放咆哮效果。
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
                    10F,
                    1F,
                    512.0
                )
                UsefulMagicPostEffects.playRgbDashBlur(it, 20, 2F, 2F)
            }
        }
        if (tick % 4 == 0) {
            instance.serverLevelApply {
                // 生成咆哮冲击波。
                ShotWaveBillboardRenderEntity.spawn(
                    it,
                    this.spawnPosition + Vec3(0.0, 15.0, 0.0),
                    fadeInTick = 10,
                    fadeOutTick = 15,
                    initialScale = 0.1F,
                    limitScale = 20F,
                    scaleSpeed = 20F,
                    rollSpeed = -0.25F,
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
    }

    override fun id(): String {
        return ID
    }
}
