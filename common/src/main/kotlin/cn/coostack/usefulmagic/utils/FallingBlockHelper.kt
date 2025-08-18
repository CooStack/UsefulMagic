package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.mixin.FallingBlockEntityAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.function.Predicate

object FallingBlockHelper {

    /**
     * @param blockList 转换成方块实体的位置列表
     * @param breakOrigin 是否破坏位置上的原先方块
     */
    fun conversionBlockToFallingBlocks(
        blockList: List<BlockPos>,
        breakOrigin: Boolean,
        world: Level
    ): List<FallingBlockEntity> {
        val res = mutableListOf<FallingBlockEntity>()
        blockList.forEach {
            if (!world.hasChunkAt(it)) return@forEach
            val state = world.getBlockState(it)
            val entity = FallingBlockEntity(EntityType.FALLING_BLOCK, world)
            entity.setPos(it.center)
            entity.disableDrop()
            (entity as FallingBlockEntityAccessor).apply {
                blockState = state
//                destroyedOnLanding = true
            }
            world.addFreshEntity(entity)
//            val entity = FallingBlockEntity.spawnFromBlock(world, it, state)
            res.add(entity)
            if (breakOrigin) {
                world.destroyBlock(it, false)
            }
        }
        return res
    }


    fun getBoxIncludeBlockPosList(
        box: AABB,
        world: Level,
        filter: Predicate<Triple<BlockState, VoxelShape, BlockPos>>
    ): List<BlockPos> {
        val res = mutableListOf<BlockPos>()
        BlockPos.betweenClosedStream(box).forEach { pos ->
            if (!world.hasChunkAt(pos)) return@forEach
            val state = world.getBlockState(pos)
            val shape = state.getCollisionShape(world, pos)
            if (filter.test(Triple(state, shape, pos))) {
                res.add(pos.immutable())
            }
        }
        return res
    }

    fun getBoxIncludeBlockPosList(box: AABB, world: Level): List<BlockPos> {
        return getBoxIncludeBlockPosList(box, world) {
            val state = it.first
            val shape = it.second
            !shape.isEmpty && !state.isAir && state.fluidState.isEmpty
        }
    }

    fun getBoxIncludeBlockPosListWithoutHardness(box: AABB, world: Level, hardMax: Float): List<BlockPos> {
        return getBoxIncludeBlockPosList(box, world) {
            val state = it.first
            val shape = it.second
            val hardness = state.getDestroySpeed(world, it.third)
            !shape.isEmpty && !state.isAir && state.fluidState.isEmpty && hardness < hardMax && hardness > 0f
        }
    }
}