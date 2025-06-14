package cn.coostack.usefulmagic.blocks

import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockCoreEntity
import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockEntity
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entitiy.UsefulMagicBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.Waterloggable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.shape.CroppedVoxelSet
import net.minecraft.util.shape.SimpleVoxelShape
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class MagicCoreBlock(settings: Settings) : BlockWithEntity(settings), Waterloggable {

    companion object {
        @JvmField
        val LEVEL = IntProperty.of("level", 0, 15)

        @JvmField
        val WATER_LOGGED = BooleanProperty.of("waterlogged")
    }


    override fun getCodec(): MapCodec<out BlockWithEntity?>? {
        return createCodec(::MagicCoreBlock)
    }

    init {
        defaultState.with(LEVEL, 15).with(WATER_LOGGED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        super.appendProperties(builder)
        builder.add(LEVEL, WATER_LOGGED)
    }

    override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float {
        return 1f
    }

    override fun createBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return MagicCoreBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return validateTicker(type, UsefulMagicBlockEntities.MAGIC_CORE) { world, pos, state, entity ->
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
        if (world.isClient) return res
        if (entity !is MagicCoreBlockEntity) {
            return res
        }
        val userStack = if (player.mainHandStack.isEmpty) player.offHandStack else player.mainHandStack
        if (userStack.isEmpty) {
            player.sendMessage(
                Text.of(
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
        }
        return ActionResult.CONSUME
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }

    override fun getRenderType(state: BlockState): BlockRenderType? {
        return BlockRenderType.INVISIBLE
    }


}