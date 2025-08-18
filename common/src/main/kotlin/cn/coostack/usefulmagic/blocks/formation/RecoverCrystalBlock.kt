package cn.coostack.usefulmagic.blocks.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class RecoverCrystalBlock(settings: BlockBehaviour.Properties) : BaseEntityBlock(settings) {
    override fun codec(): MapCodec<out BaseEntityBlock?> {
        return simpleCodec(::RecoverCrystalBlock)
    }

    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return Shapes.empty()
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    override fun <T : BlockEntity?> getTicker(
        world: Level?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return createTickerHelper(type, UsefulMagicBlockEntities.RECOVER_CRYSTAL.get()) { l, p, s, t ->
            t.tick(l, p, s)
        }
    }

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity {
        return RecoverCrystalsBlockEntity(pos, state)
    }
}