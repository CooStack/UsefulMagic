package cn.coostack.usefulmagic.blocks

import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntity
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.extend.isOf
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class AltarBlock(settings: Properties) : BaseEntityBlock(settings) {
    override fun codec(): MapCodec<out BaseEntityBlock> {
        return simpleCodec(::AltarBlock)
    }


    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return AltarBlockEntity(pos, state)
    }


    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, UsefulMagicBlockEntities.ALTAR_BLOCK.get()) { world, pos, state, entity ->
            entity.tick(world, pos, state)
        }
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): ItemInteractionResult {
        val res = super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        val entity = level.getBlockEntity(pos) ?: return res
        if (entity !is AltarBlockEntity) return res
        val altarStack = entity.getAltarStack()
        if (altarStack.isEmpty) {
            entity.setAltarStack(stack.copy().also { it.count = 1 })
            stack.count -= 1
        } else {
            if (player.inventory.add(altarStack)) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 2F, 1F)
                entity.setAltarStack(ItemStack.EMPTY)
            }
        }
        // 把玩家手上的物品塞到这里面
        return ItemInteractionResult.CONSUME_PARTIAL
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun onRemove(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (state != newState) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity !is AltarBlockEntity) {
                super.onRemove(state, world, pos, newState, moved)
                return
            }
            val item = ItemEntity(
                world, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5, blockEntity.stack
            )
            item.setPickUpDelay(40)
            world.addFreshEntity(item)
        }
        super.onRemove(state, world, pos, newState, moved)
    }


}