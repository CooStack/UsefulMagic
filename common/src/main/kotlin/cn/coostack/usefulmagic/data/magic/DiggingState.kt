package cn.coostack.usefulmagic.data.magic

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level


/**
 * 法术挖掘，设定挖掘路径用的
 *
 *
 * @property pos
 * @property progress
 * @property world level.dimension()
 * @property breakerId 独立的破坏进度ID，避免服务端把同实体ID的破坏纹理包排除给施法者自己
 */
data class DiggingState(
    val pos: BlockPos,
    var progress: Float,
    var world: ResourceKey<Level>,
    val breakerId: Int = 0
) {
    /**
     * 会计算每一个tick增加的progress
     */
    fun calculateSpeedAsLevel(actualWorld: Level, magicLevel: Int): Float {
        if (actualWorld.dimension() != world) {
            // 首先这两个位置的世界要相同否则就是挖虚空位置
            return 0f
        }

        // 找到实际的位置 方块实体
        // 如果硬度为-1则为0
        val block = actualWorld.getBlockState(pos)
        val hardness = block.getDestroySpeed(actualWorld, pos)
        if (hardness < 0f) {
            return 0f
        }
        if (hardness == 0f) return 1f
        if (magicLevel <= 0) {
            return 0f
        }
        val diggingSpeed = when (magicLevel) {
            1 -> 2.0f
            2 -> 3.0f
            3 -> 4.0f
            4 -> 6.0f
            5 -> 12.0f
            6 -> 18.0f
            else -> 24.0f
        }
        val divideSpeed = if (digDropItems(actualWorld, magicLevel)) 30f else 100f
        return diggingSpeed / hardness / divideSpeed
    }

    fun digDropItems(actualWorld: Level, magicLevel: Int): Boolean {
        val state = actualWorld.getBlockState(pos)
        return when {
            state.`is`(BlockTags.NEEDS_DIAMOND_TOOL) -> magicLevel >= 4
            state.`is`(BlockTags.NEEDS_IRON_TOOL) -> magicLevel >= 3
            state.`is`(BlockTags.NEEDS_STONE_TOOL) -> magicLevel >= 2
            else -> true
        }
    }

    fun applyProgress(actualWorld: Level, digger: Entity, magicLevel: Int) {
        if (actualWorld.dimension() != world) {
            return
        }
        if (actualWorld.isClientSide) return
        val state = actualWorld.getBlockState(pos)
        if (state.isAir) {
            actualWorld.destroyBlockProgress(progressBreakerId(digger), pos, -1)
            return
        }

        // 这里要处理方块破坏等结果
        if (progress >= 1f) {
            actualWorld.destroyBlockProgress(progressBreakerId(digger), pos, -1)
            actualWorld.destroyBlock(pos, digDropItems(actualWorld, magicLevel), digger)
            return
        }

        val stage = (progress * 10).toInt().coerceIn(0, 9)
        actualWorld.destroyBlockProgress(progressBreakerId(digger), pos, stage)
    }

    fun resetProgress(actualWorld: Level, digger: Entity) {
        if (actualWorld.isClientSide) return
        if (actualWorld.dimension() != world) {
            return
        }
        actualWorld.destroyBlockProgress(progressBreakerId(digger), pos, -1)
    }

    private fun progressBreakerId(digger: Entity): Int {
        return if (breakerId != 0) breakerId else digger.id
    }

}
