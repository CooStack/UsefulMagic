package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.platform.network.FabricClientContext
import cn.coostack.cooparticlesapi.platform.network.FabricServerContext
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlock
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlockEntityType
import cn.coostack.usefulmagic.UsefulMagicClient
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.renderer.CrystalEntityRenderer
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.renderer.FormationCoreRenderer
import cn.coostack.usefulmagic.entity.custom.renderer.MagicBookEntityRenderer
import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.gui.mana.ManaBarCallback
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.consumer.LargeManaRevive
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import cn.coostack.usefulmagic.packet.listener.client.FormationPacketListener
import cn.coostack.usefulmagic.packet.listener.client.FormationSettingsPacketResponseListener
import cn.coostack.usefulmagic.packet.listener.client.FriendChangeResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.FriendResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.ManaChangePacketListener
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import org.lwjgl.glfw.GLFW
import kotlin.collections.get

object UsefulMagicFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        loadKeyBindings()
        loadClientRegistry()
        loadScreenRenderer()
        UsefulMagicClient.init()
    }

    private fun loadScreenRenderer() {
        HudRenderCallback.EVENT.register { gui, t ->
            ManaBarCallback.onHudRender(gui, t.getGameTimeDeltaPartialTick(true))
        }
    }

    fun loadKeyBindings() {
        UsefulMagicClient.loadKeyBindings(
            KeyBindingHelper.registerKeyBinding(
                KeyMapping(
                    "key.friend_ui.open",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_J,
                    "category.ui.friend"
                )
            )
        )
        ClientTickEvents.END_CLIENT_TICK.register {
            UsefulMagicClient.tickClient()
        }
    }

    fun loadClientRegistry() {
        handleBlockRenderer()
        handleBlockLayer()
        handleNetworking()
        handleModelPredicate()
        loadEntities()
    }

    private fun loadEntities() {
        EntityRendererRegistry.register(UsefulMagicEntityTypes.METEORITE_ENTITY.get(), {
            return@register MeteoriteFallingBlockRenderer(it)
        })

        EntityRendererRegistry.register(UsefulMagicEntityTypes.FORMATION_CORE_ENTITY.get(), ::FormationCoreRenderer)
        EntityModelLayerRegistry.registerModelLayer(
            UsefulMagicEntityLayers.MAGIC_BOOK_ENTITY_LAYER,
            MagicBookEntityModel::createBodyLayer
        )

        EntityRendererRegistry.register(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE.get(),
            ::MagicBookEntityRenderer
        )
    }

    private fun handleBlockRenderer() {
        BlockEntityRenderers.register(UsefulMagicBlockEntities.ALTAR_BLOCK.get()) { AltarBlockEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.ALTAR_BLOCK_CORE.get()) { AltarBlockCoreEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.MAGIC_CORE.get()) { MagicCoreBlockEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.DEFEND_CRYSTAL.get()) { CrystalEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.SWORD_ATTACK_CRYSTAL.get()) { CrystalEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.RECOVER_CRYSTAL.get()) { CrystalEntityRenderer() }
        BlockEntityRenderers.register(UsefulMagicBlockEntities.ENERGY_CRYSTAL.get()) { CrystalEntityRenderer() }
        UsefulMagic.logger.debug("客户端方块渲染初始化完成")
    }

    private fun handleBlockLayer() {
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.ALTAR_BLOCK.get(), RenderType.cutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.get(), RenderType.cutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.get(), RenderType.cutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK.get(), RenderType.cutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.get(), RenderType.cutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.FORMATION_CORE_BLOCK.get(), RenderType.cutout())
    }

    private fun handleNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CManaDataToggle.payloadID
        ) { packet, ctx ->
            ManaChangePacketListener.receive(packet, FabricClientContext(ctx))
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFriendListResponse.payloadID
        ) { packet, ctx ->
            FriendResponsePacketListener.receive(packet, FabricClientContext(ctx))
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFriendChangeResponse.payloadID
        ) { packet, ctx -> FriendChangeResponsePacketListener.receive(packet, FabricClientContext(ctx)) }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationSettingsResponse.payloadID
        ) { packet, ctx ->
            FormationSettingsPacketResponseListener.receive(packet, FabricClientContext(ctx))
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CEnergyCrystalChange.payloadID
        ) { payload, context ->
            FormationPacketListener.handleEnergyChange(payload, FabricClientContext(context))
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationCreate.payloadID
        ) { payload, context ->
            FormationPacketListener.handleCreate(payload, FabricClientContext(context))
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationBreak.payloadID
        ) { payload, context ->
            FormationPacketListener.handleBreak(payload, FabricClientContext(context))
        }
        UsefulMagic.logger.debug("客户端自定义数据包处理器注册完成")
    }

    private fun handleModelPredicate() {
        FabricModelPredicateProviderRegistry.register(
            UsefulMagicItems.LARGE_MANA_REVIVE.getItem(),
            ResourceLocation.withDefaultNamespace("use_count"),
            { stack, world, entity, seed ->
                val count = stack.get(LARGE_REVIVE_USE_COUNT.get()) ?: LargeManaRevive.MAX_USAGE
                1f - (count.toFloat() / LargeManaRevive.MAX_USAGE)
            }
        )
        UsefulMagic.logger.debug("模型谓词注册完成")
    }
}