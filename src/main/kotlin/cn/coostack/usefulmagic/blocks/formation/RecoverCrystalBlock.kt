package cn.coostack.usefulmagic.blocks.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
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

class RecoverCrystalBlock(settings: Settings) : BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity?>? {
        return createCodec(::RecoverCrystalBlock)
    }
    override fun getCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return VoxelShapes.empty()
    }
    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.INVISIBLE
    }
    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return validateTicker(
            type, UsefulMagicBlockEntities.RECOVER_CRYSTAL
        ) { w, p, s, e ->
            e.tick(w, p, s)
        }
    }

    override fun createBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return RecoverCrystalsBlockEntity(pos, state)
    }
}