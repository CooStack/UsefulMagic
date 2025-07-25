package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.control.group.ClientParticleGroupManager
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entitiy.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.renderer.MagicBookEntityRenderer
import cn.coostack.usefulmagic.gui.ManaBarCallback
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.consumer.LargeManaRevive
import cn.coostack.usefulmagic.managers.ClientManaManager
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import cn.coostack.usefulmagic.packet.listener.ManaChangePacketListener
import cn.coostack.usefulmagic.particles.emitters.UsefulMagicEmitters
import cn.coostack.usefulmagic.particles.group.client.EnchantBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.GoldenBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.SingleBarrageParticleClient
import cn.coostack.usefulmagic.particles.style.AntiEntityWandBarrageStyle
import cn.coostack.usefulmagic.particles.style.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.AntiEntityWandStyle
import cn.coostack.usefulmagic.particles.style.CopperMagicStyle
import cn.coostack.usefulmagic.particles.style.DiamondWandStyle
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.particles.style.EndRodExplosionStyle
import cn.coostack.usefulmagic.particles.style.EndRodLineStyle
import cn.coostack.usefulmagic.particles.style.GoldenWandStyle
import cn.coostack.usefulmagic.particles.style.HealthReviveStyle
import cn.coostack.usefulmagic.particles.style.LightStyle
import cn.coostack.usefulmagic.particles.style.NetheriteWandStyle
import cn.coostack.usefulmagic.particles.style.skill.TaiChiStyle
import cn.coostack.usefulmagic.particles.style.WandMeteoriteStyle
import cn.coostack.usefulmagic.particles.style.WandMeteoriteSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.WandMeteoriteTargetStyle
import cn.coostack.usefulmagic.particles.style.TestStyle
import cn.coostack.usefulmagic.particles.style.entitiy.BookEntityDeathStyle
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel1Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel2Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel3Style
import cn.coostack.usefulmagic.particles.style.entitiy.MagicBookSpawnStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import cn.coostack.usefulmagic.particles.style.skill.BookShootSkillStyle
import cn.coostack.usefulmagic.particles.style.skill.GiantSwordStyle
import cn.coostack.usefulmagic.particles.style.skill.SwordLightStyle
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.util.Identifier

object UsefulMagicClient : ClientModInitializer {
    override fun onInitializeClient() {
        handleNetworking()
        loadParticleStyles()
        loadScreenRenderer()
        handleModelPredicate()
        handleBlockRenderer()
        handleClientEvents()
        loadEntities()
        UsefulMagic.logger.debug("模组开发: 禁止压入的空栈!")
        UsefulMagic.logger.debug("粉丝群: 1108818905")
        UsefulMagic.logger.debug("此版本为免费版本 如果你是付费得到此模组 那么你被骗了")
        UsefulMagic.logger.debug("B站: 禁止压入的空栈")
        UsefulMagic.logger.debug("抖音: 禁止压入的空栈!")
        UsefulMagic.logger.debug("小红书: 禁止压入的空栈!")
        UsefulMagic.logger.debug("支付宝生活好评: 禁止压入的空栈!")
        UsefulMagic.logger.debug("感谢各位的支持!")
    }

    private fun loadEntities() {
        EntityRendererRegistry.register(MeteoriteFallingBlockEntity.ENTITY_TYPE, {
            return@register MeteoriteFallingBlockRenderer(it)
        })

        EntityModelLayerRegistry.registerModelLayer(
            UsefulMagicEntityLayers.MAGIC_BOOK_ENTITY_LAYER,
            MagicBookEntityModel::getTexturedModelData
        )

        EntityRendererRegistry.register(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE,
            ::MagicBookEntityRenderer
        )

    }

    private fun loadParticleStyles() {
        ClientParticleGroupManager.register(
            SingleBarrageParticleClient::class.java,
            SingleBarrageParticleClient.Provider()
        )
        ClientParticleGroupManager.register(
            EnchantBallBarrageParticleClient::class.java,
            EnchantBallBarrageParticleClient.Provider()
        )
        ClientParticleGroupManager.register(
            GoldenBallBarrageParticleClient::class.java,
            GoldenBallBarrageParticleClient.Provider()
        )

        ParticleStyleManager.register(
            CopperMagicStyle::class.java, CopperMagicStyle.Provider()
        )

        ParticleStyleManager.register(GoldenWandStyle::class.java, GoldenWandStyle.Provider())
        ParticleStyleManager.register(HealthReviveStyle::class.java, HealthReviveStyle.Provider())
        ParticleStyleManager.register(EnchantLineStyle::class.java, EnchantLineStyle.Provider())
        ParticleStyleManager.register(LightStyle::class.java, LightStyle.Provider())
        ParticleStyleManager.register(TestStyle::class.java, TestStyle.Provider())
        ParticleStyleManager.register(EndRodSwordStyle::class.java, EndRodSwordStyle.Provider())
        ParticleStyleManager.register(DiamondWandStyle::class.java, DiamondWandStyle.Provider())
        ParticleStyleManager.register(WandMeteoriteStyle::class.java, WandMeteoriteStyle.Provider())
        ParticleStyleManager.register(WandMeteoriteTargetStyle::class.java, WandMeteoriteTargetStyle.Provider())
        ParticleStyleManager.register(
            WandMeteoriteSpellcasterStyle::class.java,
            WandMeteoriteSpellcasterStyle.Provider()
        )
        ParticleStyleManager.register(EndRodExplosionStyle::class.java, EndRodExplosionStyle.Provider())
        ParticleStyleManager.register(NetheriteWandStyle::class.java, NetheriteWandStyle.Provider())
        ParticleStyleManager.register(
            AntiEntityWandSpellcasterStyle::class.java,
            AntiEntityWandSpellcasterStyle.Provider()
        )
        ParticleStyleManager.register(AntiEntityWandStyle::class.java, AntiEntityWandStyle.Provider())
        ParticleStyleManager.register(AntiEntityWandBarrageStyle::class.java, AntiEntityWandBarrageStyle.Provider())
        ParticleStyleManager.register(EndRodLineStyle::class.java, EndRodLineStyle.Provider())
        ParticleStyleManager.register(CraftingLevel1Style::class.java, CraftingLevel1Style.Provider())
        ParticleStyleManager.register(CraftingLevel2Style::class.java, CraftingLevel2Style.Provider())
        ParticleStyleManager.register(CraftingLevel3Style::class.java, CraftingLevel3Style.Provider())
        ParticleStyleManager.register(ExplosionStarStyle::class.java, ExplosionStarStyle.Provider())
        ParticleStyleManager.register(ExplosionMagicStyle::class.java, ExplosionMagicStyle.Provider())
        ParticleStyleManager.register(ExplosionMagicBallStyle::class.java, ExplosionMagicBallStyle.Provider())
        ParticleStyleManager.register(TaiChiStyle::class.java, TaiChiStyle.Provider())
        ParticleStyleManager.register(BookShootSkillStyle::class.java, BookShootSkillStyle.Provider())
        ParticleStyleManager.register(BookEntityDeathStyle::class.java, BookEntityDeathStyle.Provider())
        ParticleStyleManager.register(MagicBookSpawnStyle::class.java, MagicBookSpawnStyle.Provider())
        ParticleStyleManager.register(SwordLightStyle::class.java, SwordLightStyle.Provider())
        ParticleStyleManager.register(GiantSwordStyle::class.java, GiantSwordStyle.Provider())
        UsefulMagicEmitters.init()
        UsefulMagic.logger.debug("客户端粒子样式注册完成")
    }

    private fun handleBlockRenderer() {
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.ALTAR_BLOCK, RenderLayer.getCutout())
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.ALTAR_BLOCK) { AltarBlockEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.ALTAR_BLOCK_CORE) { AltarBlockCoreEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.MAGIC_CORE) { MagicCoreBlockEntityRenderer() }
        UsefulMagic.logger.debug("客户端方块渲染初始化完成")
    }

    private fun handleClientEvents() {
        ClientPlayConnectionEvents.JOIN.register { h, _, c ->
            val actualUUID = c.player?.uuid ?: return@register
            ClientManaManager.data.owner = actualUUID
        }
    }

    private fun loadScreenRenderer() {
        HudRenderCallback.EVENT.register(ManaBarCallback())
    }

    private fun handleNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CManaDataToggle.payloadID, ManaChangePacketListener
        )
        UsefulMagic.logger.debug("客户端自定义数据包处理器注册完成")
    }


    private fun handleModelPredicate() {
        ModelPredicateProviderRegistry.register(
            UsefulMagicItems.LARGE_MANA_REVIVE,
            Identifier.ofVanilla("use_count"),
            { stack, world, entity, seed ->
                val count = stack.get(LARGE_REVIVE_USE_COUNT) ?: LargeManaRevive.MAX_USAGE
                1f - (count.toFloat() / LargeManaRevive.MAX_USAGE)
            }
        )
        UsefulMagic.logger.debug("模型谓词注册完成")
    }
}