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
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FormationCoreBlock(settings: Settings) : BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity?>? {
        return createCodec(::FormationCoreBlock)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T?>?
    ): BlockEntityTicker<T?>? {
        return validateTicker(type, UsefulMagicBlockEntities.FORMATION_CORE) { w, p, s, e ->
            e.tick(w, p, s)
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.MODEL
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult
    ): ActionResult {
        // 判断是否可以通过阵法构造
        val entity = world.getBlockEntity(pos) ?: return super.onUse(state, world, pos, player, hit)
        if (entity !is FormationCoreBlockEntity) {
            return super.onUse(state, world, pos, player, hit)
        }
        if (world.isClient) {
            UsefulMagic.logger.info("try send request")
            ClientRequestManager.sendRequest(
                PacketC2SFormationSettingRequest(pos), PacketS2CFormationSettingsResponse.payloadID
            ).recall {
                it as PacketS2CFormationSettingsResponse
                if (!it.isOwner) {
                    UsefulMagic.logger.info("request failed")
                    return@recall
                }
                UsefulMagic.logger.info("try set screen")
                MinecraftClient.getInstance().executeTask {
                    MinecraftClient.getInstance().setScreen(
                        FormationSettingScreen(pos, it.settings)
                    )
                }
            }
            return ActionResult.SUCCESS_NO_ITEM_USED
        }
        val notHoldItem = player.handItems.all { it.isEmpty }
        if (!notHoldItem) {
            return super.onUse(state, world, pos, player, hit)
        }
        val formation = entity.formation
        if (formation.canBeFormation() && !formation.isActiveFormation() && !ServerFormationManager.checkPosInFormationRange(
                pos.toCenterPos(),
                world as ServerWorld
            )
        ) {
            formation.owner = player.uuid
            formation.tryBuildFormation()
            // 激活成功
            return ActionResult.SUCCESS_NO_ITEM_USED
        }

        return super.onUse(state, world, pos, player, hit)
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState? {
        if (world.isClient) {
            return super.onBreak(world, pos, state, player)
        }
        val entity = world.getBlockEntity(pos) as FormationCoreBlockEntity
        entity.formation.breakFormation(Float.MAX_VALUE, null)
        return super.onBreak(world, pos, state, player)
    }

    override fun createBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity {
        return FormationCoreBlockEntity(pos, state)
    }
}