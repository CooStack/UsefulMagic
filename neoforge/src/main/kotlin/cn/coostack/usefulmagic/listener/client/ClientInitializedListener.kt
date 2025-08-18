package cn.coostack.usefulmagic.listener.client

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicClient
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.renderer.CrystalEntityRenderer
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.MagicBookEntityModel.createBodyLayer
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.renderer.FormationCoreRenderer
import cn.coostack.usefulmagic.entity.custom.renderer.MagicBookEntityRenderer
import cn.coostack.usefulmagic.gui.mana.ManaBarCallback
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RenderGuiEvent
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = UsefulMagic.MOD_ID)
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