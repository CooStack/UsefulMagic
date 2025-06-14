package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.mixin.FallingBlockEntityAccessor
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.World
import java.util.function.Predicate

object FallingBlockHelper {

    /**
     * @param blockList 转换成方块实体的位置列表
     * @param breakOrigin 是否破坏位置上的原先方块
     */
    fun conversionBlockToFallingBlocks(
        blockList: List<BlockPos>,
        breakOrigin: Boolean,
        world: World
    ): List<FallingBlockEntity> {
        val res = mutableListOf<FallingBlockEntity>()
        blockList.forEach {
            if (!world.isChunkLoaded(it)) return@forEach
            val state = world.getBlockState(it)
            val entity = FallingBlockEntity(EntityType.FALLING_BLOCK, world)
            entity.setPosition(it.toCenterPos())
            (entity as FallingBlockEntityAccessor).apply {
                blockState = state
                destroyedOnLanding = true
            }
            world.spawnEntity(entity)
//            val entity = FallingBlockEntity.spawnFromBlock(world, it, state)
            res.add(entity)
            if (breakOrigin) {
                world.breakBlock(it, false)
            }
        }
        return res
    }


    fun getBoxIncludeBlockPosList(
        box: Box,
        world: World,
        filter: Predicate<Triple<BlockState, VoxelShape, BlockPos>>
    ): List<BlockPos> {
        val res = mutableListOf<BlockPos>()
        BlockPos.stream(box).forEach {
            if (!world.isChunkLoaded(it)) return@forEach
            val state = world.getBlockState(it)
            val shape = state.getCollisionShape(world, it)
            if (filter.test(Triple(state, shape, it))) {
                res.add(it.mutableCopy())
            }
        }
        return res
    }

    fun getBoxIncludeBlockPosList(box: Box, world: World): List<BlockPos> {
        return getBoxIncludeBlockPosList(box, world) {
            val state = it.first
            val shape = it.second
            !shape.isEmpty && !state.isAir && !state.isLiquid
        }
    }

    fun getBoxIncludeBlockPosListWithoutHardness(box: Box, world: World, hardMax: Float): List<BlockPos> {
        return getBoxIncludeBlockPosList(box, world) {
            val state = it.first
            val shape = it.second
            val hardness = state.getHardness(world, it.third)
            !shape.isEmpty && !state.isAir && !state.isLiquid && hardness < hardMax && hardness > 0f
        }
    }
}