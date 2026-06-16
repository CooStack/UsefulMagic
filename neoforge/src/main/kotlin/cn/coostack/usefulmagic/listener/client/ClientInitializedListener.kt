package cn.coostack.usefulmagic.listener.client

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicClient
import cn.coostack.usefulmagic.blocks.entity.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.renderer.CrystalEntityRenderer
import cn.coostack.usefulmagic.client.tooltip.NeoLoadedMagicClientTooltip
import cn.coostack.usefulmagic.client.tooltip.NeoSpellBagClientTooltip
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.renderer.*
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.prop.SpellBagTooltip
import cn.coostack.usefulmagic.items.weapon.wands.LoadedMagicTooltip
import cn.coostack.usefulmagic.particles.particle.UsefulMagicParticleTypes
import cn.coostack.usefulmagic.particles.particle.WaveParticleProvider
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
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
            "category.usefulmagic.keys"
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
        event.registerEntityRenderer(UsefulMagicEntityTypes.MAGIC_DRAGON_ENTITY_TYPE.get(), ::MagicDragonRenderer)
        event.registerEntityRenderer(UsefulMagicEntityTypes.MAGIC_EYE_ENTITY_TYPE.get(), ::MagicEyeEntityRenderer)
        event.registerEntityRenderer(UsefulMagicEntityTypes.MAGIC_SUB_EYE_ENTITY_TYPE.get(), ::MagicSubEyeEntityRenderer)
        event.registerEntityRenderer(UsefulMagicEntityTypes.MAGIC_HEART_ENTITY_TYPE.get(), ::MagicHeartEntityRenderer)
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

    @SubscribeEvent
    fun registerTooltipComponent(event: RegisterClientTooltipComponentFactoriesEvent) {
        event.register(LoadedMagicTooltip::class.java) { tooltip ->
            NeoLoadedMagicClientTooltip(tooltip.magicStack)
        }
        event.register(SpellBagTooltip::class.java) { tooltip ->
            NeoSpellBagClientTooltip(tooltip)
        }
    }

    @SubscribeEvent
    fun registerParticles(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(UsefulMagicParticleTypes.WAVE_PARTICLE.get()) { sprites ->
            WaveParticleProvider(sprites)
        }
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
