package cn.coostack.usefulmagic.blocks.formation

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.gui.formation.FormationSettingScreen
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import com.mojang.serialization.MapCodec
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class FormationCoreBlock(settings: BlockBehaviour.Properties) : BaseEntityBlock(settings) {
    override fun codec(): MapCodec<out BaseEntityBlock?> {
        return simpleCodec(::FormationCoreBlock)
    }

    override fun <T : BlockEntity?> getTicker(
        world: Level?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return createTickerHelper(type, UsefulMagicBlockEntities.FORMATION_CORE.get()) { w, p, s, e ->
            e.tick(w, p, s)
        }
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        // 判断是否可以通过阵法构造
        val entity = world.getBlockEntity(pos) ?: return super.useWithoutItem(state, world, pos, player, hit)
        if (entity !is FormationCoreBlockEntity) {
            return super.useWithoutItem(state, world, pos, player, hit)
        }
        if (world.isClientSide) {
            UsefulMagic.logger.debug("try send request")
            ClientRequestManager.sendRequest(
                PacketC2SFormationSettingRequest(pos), PacketS2CFormationSettingsResponse.payloadID
            ).recall {
                it as PacketS2CFormationSettingsResponse
                if (!it.isOwner) {
                    UsefulMagic.logger.debug("request failed")
                    return@recall
                }
                UsefulMagic.logger.debug("try set screen")
                Minecraft.getInstance().execute {
                    Minecraft.getInstance().setScreen(
                        FormationSettingScreen(pos, it.settings)
                    )
                }
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED
        }


        val formation = entity.formation
        if (formation.canBeFormation() && !formation.isActiveFormation() && !ServerFormationManager.checkPosInFormationRange(
                pos.center,
                world as ServerLevel
            )
        ) {
            formation.owner = player.uuid
            formation.tryBuildFormation()
            // 激活成功
            return InteractionResult.SUCCESS_NO_ITEM_USED
        }

        return super.useWithoutItem(state, world, pos, player, hit)
    }


    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!world.isClientSide) {
            val entity = world.getBlockEntity(pos) as? FormationCoreBlockEntity
            entity?.formation?.breakFormation(Float.MAX_VALUE, null)
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return FormationCoreBlockEntity(pos, state)
    }
}