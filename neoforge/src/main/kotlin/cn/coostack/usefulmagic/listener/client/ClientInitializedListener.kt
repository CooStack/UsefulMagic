package cn.coostack.usefulmagic.listener.client

import cn.coostack.cooparticlesapi.platform.network.NeoForgeClientContext
import cn.coostack.cooparticlesapi.platform.network.NeoForgeServerContext
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicClient
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
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendAddRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
import cn.coostack.usefulmagic.packet.listener.client.FormationPacketListener
import cn.coostack.usefulmagic.packet.listener.client.FormationSettingsPacketResponseListener
import cn.coostack.usefulmagic.packet.listener.client.FriendChangeResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.FriendResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.ManaChangePacketListener
import cn.coostack.usefulmagic.packet.listener.server.FormationSettingChangePacketListener
import cn.coostack.usefulmagic.packet.listener.server.FormationSettingRequestPacketListener
import cn.coostack.usefulmagic.packet.listener.server.FriendAddListRequestHandler
import cn.coostack.usefulmagic.packet.listener.server.FriendListRequestHandler
import cn.coostack.usefulmagic.packet.listener.server.FriendRemoveListRequestHandler
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.registries.RegisterEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = UsefulMagic.MOD_ID, value = [Dist.CLIENT])
object ClientInitializedListener {
    @SubscribeEvent
    fun onKeybinding(event: RegisterKeyMappingsEvent) {
        val binding = KeyMapping(
            "key.friend_ui.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.ui.friend"
        )
        event.register(binding)
        UsefulMagicClient.loadKeyBindings(binding)
    }

    @SubscribeEvent
    fun onClickRegister(event: RegisterEvent) {
        event.register(BuiltInRegistries.CREATIVE_MODE_TAB.key()) {
            val group = UsefulMagicItemGroups.usefulMagicMainGroup
            it.register(group.id, group.get())
        }
    }

    @SubscribeEvent
    fun registerEntityRender(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(
            UsefulMagicEntityTypes.METEORITE_ENTITY.get()
        ) {
            return@registerEntityRenderer MeteoriteFallingBlockRenderer(it)
        }
        event.registerEntityRenderer(UsefulMagicEntityTypes.FORMATION_CORE_ENTITY.get(), ::FormationCoreRenderer)
        event.registerEntityRenderer(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE.get(),
            ::MagicBookEntityRenderer
        )
        handleBlockEntity(event)
    }


    @SubscribeEvent
    fun registerEntityLayer(event: EntityRenderersEvent.RegisterLayerDefinitions) {
        event.registerLayerDefinition(
            UsefulMagicEntityLayers.MAGIC_BOOK_ENTITY_LAYER,
            MagicBookEntityModel::createBodyLayer
        )
    }

    private fun handleBlockEntity(event: EntityRenderersEvent.RegisterRenderers) {

        UsefulMagicBlockEntities.reg()

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.ALTAR_BLOCK.get()
        ) { AltarBlockEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.ALTAR_BLOCK_CORE.get()
        ) { AltarBlockCoreEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.MAGIC_CORE.get()
        ) { MagicCoreBlockEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.DEFEND_CRYSTAL.get()
        ) { CrystalEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.SWORD_ATTACK_CRYSTAL.get()
        ) { CrystalEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.RECOVER_CRYSTAL.get()
        ) { CrystalEntityRenderer() }

        event.registerBlockEntityRenderer(
            UsefulMagicBlockEntities.ENERGY_CRYSTAL.get()
        ) { CrystalEntityRenderer() }
    }


}