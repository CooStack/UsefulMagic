package cn.coostack.usefulmagic.blocks

import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class MagicCoreBlock(settings: Properties) : BaseEntityBlock(settings), SimpleWaterloggedBlock {

    companion object {
        @JvmField
        val LEVEL: IntegerProperty = IntegerProperty.create("level", 0, 15)

        @JvmField
        val WATER_LOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
    }


    override fun codec(): MapCodec<out BaseEntityBlock?> {
        return simpleCodec(::MagicCoreBlock)
    }

    init {
        defaultBlockState().setValue(LEVEL, 15)
            .setValue(WATER_LOGGED, false)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LEVEL, WATER_LOGGED)
    }


    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return MagicCoreBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: Level?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return createTickerHelper(type, UsefulMagicBlockEntities.MAGIC_CORE.get()) { world, pos, state, entity ->
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
        if (level.isClientSide) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        val entity = level.getBlockEntity(pos)
        if (entity !is MagicCoreBlockEntity) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }
        if (!stack.isEmpty) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }
        if (!entity.checkCompleteness()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        }
        if (hand != InteractionHand.MAIN_HAND) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        player.sendSystemMessage(
            Component.literal(
                """
                        §a 魔力核心-属性
                        §7| §f当前魔力值: ${entity.currentMana}
                        §7| §f最大储存魔力值: ${entity.maxMana}
                        §7| §f合成进度: ${
                    if (entity.crafting) {
                        "${"%.2d".format(entity.craftingTick.toDouble() / (entity.currentRecipe?.tick ?: 1))}%"
                    } else {
                        "未发现合成配方"
                    }
                }
                        §7| §f魔力恢复速度:${entity.currentReviveSpeed}
                    """.trimIndent()
            )
        )

        return ItemInteractionResult.CONSUME
    }


    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }


}