package cn.coostack.usefulmagic.entity.custom.dragon.eye.phases

import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class TeleportRandomlyPhase : PhaseDefinition<MagicEyeEntity> {
    companion object {
        const val ID = "teleport-randomly-phase"
        val HOLDER = PhaseRegistries.register(ID) { TeleportRandomlyPhase() }
    }

    override fun canBegin(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return true
    }

    override fun begin(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun step(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        // 传送到target周围
        val original = runtime.peekTargetOrNull() ?: return PhaseResult.Reset
        val teleportTarget = original.offsetRandomly(5.0)
        //  检测位置合理性
        val world = instance.level()
        val blockPos = BlockPos.containing(teleportTarget)
            .mutable()
        var retryCount = 3
        while (!world.getBlockState(blockPos).isAir && retryCount-- > 0) {
            blockPos.move(0, 1, 0)
        }
        val final = world.getBlockState(blockPos).isAir
        if (final) {
            blockPos.move(0, Random.nextInt(2, 4), 0)
            blockPos.center.apply {
                instance.teleportTo(x, y, z)
            }
            instance.playSound(SoundEvents.PLAYER_TELEPORT, 5f, 1f)
        }
        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicEyeEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun id(): String {
        return ID
    }
}
