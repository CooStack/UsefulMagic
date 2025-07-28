package cn.coostack.usefulmagic.blocks.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.SwordAttackCrystalsBlockEntity
import com.mojang.serialization.MapCodec
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class SwordAttackCrystalsBlock(settings: Settings) : BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity> {
        return createCodec(::SwordAttackCrystalsBlock)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.INVISIBLE
    }

    override fun getCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return VoxelShapes.empty()
    }

    override fun createBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity {
        return SwordAttackCrystalsBlockEntity(pos, state)
    }
}