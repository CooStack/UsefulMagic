package cn.coostack.usefulmagic.blocks

import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntity
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class AltarBlock(settings: Settings) : BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity?>? {
        return createCodec(::AltarBlock)
    }

    override fun createBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return AltarBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return validateTicker(type, UsefulMagicBlockEntities.ALTAR_BLOCK) { world, pos, state, entity ->
            entity.tick(world, pos, state)
        }
    }
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult
    ): ActionResult? {
        val res = super.onUse(state, world, pos, player, hit)
        val entity = world.getBlockEntity(pos) ?: return res
        if (entity !is AltarBlockEntity) {
            return res
        }
        val handStack = entity.stack
        val user = player
        val userStack = if (user.mainHandStack.isEmpty) user.offHandStack else user.mainHandStack
        if (handStack.isEmpty) {
            // 把玩家手上的物品塞到这里面
            if (userStack.isEmpty) return res
            entity.setAltarStack(userStack.copy().also { it.count = 1 })
            userStack.decrement(1)
        } else {
            // 添加到背包
            val stack = entity.stack
            if (user.inventory.insertStack(stack)) {
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 2F, 1F)
                entity.setAltarStack(ItemStack.EMPTY)
            }
        }
        return ActionResult.CONSUME
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.MODEL
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (state != newState) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity !is AltarBlockEntity) {
                super.onStateReplaced(state, world, pos, newState, moved)
                return
            }
            val item = ItemEntity(
                world, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5, blockEntity.stack
            )
            item.setPickupDelay(40)
            world.spawnEntity(item)
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

}