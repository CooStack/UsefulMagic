package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.control.group.ClientParticleGroupManager
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.renderer.CrystalEntityRenderer
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.renderer.MagicBookEntityRenderer
import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.gui.mana.ManaBarCallback
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.consumer.LargeManaRevive
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import cn.coostack.usefulmagic.packet.listener.client.FormationPacketListener
import cn.coostack.usefulmagic.packet.listener.client.FormationSettingsPacketResponseListener
import cn.coostack.usefulmagic.packet.listener.client.FriendChangeResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.FriendResponsePacketListener
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import cn.coostack.usefulmagic.packet.listener.server.ManaChangePacketListener
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import cn.coostack.usefulmagic.particles.emitters.UsefulMagicEmitters
import cn.coostack.usefulmagic.particles.fall.style.GuildCircleStyle
import cn.coostack.usefulmagic.particles.group.client.EnchantBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.GoldenBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.SingleBarrageParticleClient
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandBarrageStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.CopperMagicStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.DiamondWandStyle
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.particles.style.EndRodExplosionStyle
import cn.coostack.usefulmagic.particles.style.EndRodLineStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.GoldenWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.HealthReviveStyle
import cn.coostack.usefulmagic.particles.style.LightStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.NetheriteWandStyle
import cn.coostack.usefulmagic.particles.style.skill.TaiChiStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteTargetStyle
import cn.coostack.usefulmagic.particles.style.TestStyle
import cn.coostack.usefulmagic.particles.style.entitiy.BookEntityDeathStyle
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel1Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel2Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel3Style
import cn.coostack.usefulmagic.particles.style.entitiy.MagicBookSpawnStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.particles.style.formation.LargeFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.MidFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.SmallFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.SwordAttackCrystalStyle
import cn.coostack.usefulmagic.particles.style.skill.BookShootSkillStyle
import cn.coostack.usefulmagic.particles.style.skill.GiantSwordStyle
import cn.coostack.usefulmagic.particles.style.skill.SwordLightStyle
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object UsefulMagicClient : ClientModInitializer {
    lateinit var friendUIBinding: KeyBinding
    override fun onInitializeClient() {
        handleNetworking()
        loadParticleStyles()
        loadScreenRenderer()
        handleModelPredicate()
        handleBlockRenderer()
        handleBlockLayer()
        handleClientEvents()
        loadEntities()
        printLogger()
        loadEvents()
        loadKeyBindings()
    }

    private fun loadKeyBindings() {
        friendUIBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.friend_ui.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.ui.friend"
            )
        )
        ClientTickEvents.END_CLIENT_TICK.register {
            if (friendUIBinding.wasPressed()) {
                it.setScreen(FriendManagerScreen())
            }
        }
    }

    private fun loadEvents() {
        fun text(msg: String): Text = Text.literal("[UsefulMagic] $msg")
        ClientPlayConnectionEvents.JOIN.register { handler, sender, client ->
            client.player?.let {
                it.sendMessage(text("模组开发: 禁止压入的空栈!"))
                it.sendMessage(text("模组开发: 1108818905"))
                it.sendMessage(text("此模组是免费发放的,如果你是付费得到的模组,那么你大概率被骗了"))
                it.sendMessage(text("B站: 禁止压入的空栈"))
                it.sendMessage(text("抖音: 禁止压入的空栈!"))
                it.sendMessage(text("小红书: 禁止压入的空栈!"))
                it.sendMessage(text("支付宝生活号: 禁止压入的空栈!"))
                it.sendMessage(text("百家号: 禁止压入的空栈"))
                it.sendMessage(text("感谢各位的支持!"))
            }
        }
    }

    private fun printLogger() {
        UsefulMagic.logger.debug("模组开发: 禁止压入的空栈!")
        UsefulMagic.logger.debug("粉丝群: 1108818905")
        UsefulMagic.logger.debug("此版本为免费版本 如果你是付费得到此模组 那么你被骗了")
        UsefulMagic.logger.debug("B站: 禁止压入的空栈")
        UsefulMagic.logger.debug("抖音: 禁止压入的空栈!")
        UsefulMagic.logger.debug("小红书: 禁止压入的空栈!")
        UsefulMagic.logger.debug("支付宝生活号: 禁止压入的空栈!")
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
        ParticleStyleManager.register(DefendCrystalStyle::class.java, DefendCrystalStyle.Provider())
        ParticleStyleManager.register(RecoverCrystalStyle::class.java, RecoverCrystalStyle.Provider())
        ParticleStyleManager.register(SwordAttackCrystalStyle::class.java, SwordAttackCrystalStyle.Provider())
        ParticleStyleManager.register(SmallFormationStyle::class.java, SmallFormationStyle.Provider())
        ParticleStyleManager.register(MidFormationStyle::class.java, MidFormationStyle.Provider())
        ParticleStyleManager.register(LargeFormationStyle::class.java, LargeFormationStyle.Provider())
        ParticleStyleManager.register(SkyFallingStyle::class.java, SkyFallingStyle.Provider())
        ParticleStyleManager.register(GuildCircleStyle::class.java, GuildCircleStyle.Provider())
        UsefulMagicEmitters.init()
        UsefulMagic.logger.debug("客户端粒子样式注册完成")
    }

    private fun handleBlockRenderer() {
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.ALTAR_BLOCK, RenderLayer.getCutout())
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.ALTAR_BLOCK) { AltarBlockEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.ALTAR_BLOCK_CORE) { AltarBlockCoreEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.MAGIC_CORE) { MagicCoreBlockEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.DEFEND_CRYSTAL) { CrystalEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.SWORD_ATTACK_CRYSTAL) { CrystalEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.RECOVER_CRYSTAL) { CrystalEntityRenderer() }
        BlockEntityRendererFactories.register(UsefulMagicBlockEntities.ENERGY_CRYSTAL) { CrystalEntityRenderer() }
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


    private fun handleBlockLayer() {
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(UsefulMagicBlocks.FORMATION_CORE_BLOCK, RenderLayer.getCutout())
    }

    private fun handleNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CManaDataToggle.payloadID, ManaChangePacketListener
        )
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFriendListResponse.payloadID, FriendResponsePacketListener
        )
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFriendChangeResponse.payloadID, FriendChangeResponsePacketListener
        )
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationSettingsResponse.payloadID, FormationSettingsPacketResponseListener
        )
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CEnergyCrystalChange.payloadID
        ) { payload, context ->
            FormationPacketListener.handleEnergyChange(payload, context)
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationCreate.payloadID
        ) { payload, context ->
            FormationPacketListener.handleCreate(payload, context)
        }
        ClientPlayNetworking.registerGlobalReceiver(
            PacketS2CFormationBreak.payloadID
        ) { payload, context ->
            FormationPacketListener.handleBreak(payload, context)
        }

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