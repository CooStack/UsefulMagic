package cn.coostack.usefulmagic.blocks.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.DefendCrystalBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.CubeVoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class DefendCrystalBlock(settings: Properties) : BaseEntityBlock(settings) {
    override fun codec(): MapCodec<out BaseEntityBlock> {
        return simpleCodec(::DefendCrystalBlock)
    }


    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T?>? {
        return createTickerHelper(blockEntityType, UsefulMagicBlockEntities.DEFEND_CRYSTAL.get()) { w, p, s, e ->
            e.tick()
        }

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

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return DefendCrystalBlockEntity(pos, state)
    }
}