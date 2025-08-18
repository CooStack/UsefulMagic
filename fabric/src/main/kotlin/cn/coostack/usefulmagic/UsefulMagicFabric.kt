package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.platform.network.FabricServerContext
import cn.coostack.usefulmagic.UsefulMagic.logger
import cn.coostack.usefulmagic.UsefulMagic.state
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.listener.DefendMagicListener
import cn.coostack.usefulmagic.meteorite.MeteoriteManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendAddRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
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
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

object UsefulMagicFabric : ModInitializer {
    override fun onInitialize() {
        UsefulMagic.init()
        loadTickers()
        registerPackets()
        initRegistries()
        loadPacketListener()
        loadEntityAttributes()
        UsefulMagicBlockEntities.reg()
        loadFuel()
    }

    private fun loadTickers() {
        ServerTickEvents.START_SERVER_TICK.register { s ->
            UsefulMagic.tickServer()
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            UsefulMagic.setupServer(server)
        }
        ServerPlayConnectionEvents.JOIN.register { h, _, _ ->
            val player = h.player
            state.getDataFromServer(player.uuid)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            val player = handler.player ?: return@register
            SkyFallingRuneItem.playerMagicStyles.remove(player.uuid)
            SkyFallingRuneItem.playerTasks.remove(player.uuid)
        }
        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            MeteoriteManager.clearAll()
        }
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            DefendMagicListener.call(entity, source, amount)
        }
        logger.debug("服务器随机刻加载完成")
    }

    private fun loadEntityAttributes() {
        UsefulMagicEntityTypes.init()
        FabricDefaultAttributeRegistry.register(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE.get(),
            MagicBookEntity.createDefaultMobAttributes()
        )
    }

    private fun initRegistries() {
        UsefulMagicBlocks.blocks.forEach {
            Registry.register(BuiltInRegistries.BLOCK, it.id, it.get())
        }
        UsefulMagicBlocks.blockItems.forEach {
            Registry.register(BuiltInRegistries.ITEM, it.id, it.getItem())
        }
        UsefulMagicBlockEntities.blockEntityTypes.forEach {
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, it.id, it.get())
        }

        UsefulMagicRecipeTypes.recipeTypes.forEach {
            Registry.register(BuiltInRegistries.RECIPE_TYPE, it.id, it.get())
        }
        UsefulMagicRecipeTypes.recipeSerializer.forEach {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, it.id, it.get())
        }
        UsefulMagicEntityTypes.entityTypes.forEach {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, it.id, it.get())
        }

        UsefulMagicSoundEvents.soundEvents.forEach {
            Registry.register(BuiltInRegistries.SOUND_EVENT, it.id, it.get())
        }

        UsefulMagicItems.items.forEach {
            Registry.register(BuiltInRegistries.ITEM, it.id, it.getItem())
        }
        val group = UsefulMagicItemGroups.usefulMagicMainGroup
        Registry.register(group.type, group.id, group.get())

        UsefulMagicDataComponentTypes.types.forEach {
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, it.id, it.get())
        }
        logger.debug("注册表输入成功")
    }

    private fun loadFuel() {
        FuelRegistry.INSTANCE.add(UsefulMagicItems.WOODEN_WAND.getItem(), 200)
    }

    private fun registerPackets() {
        PayloadTypeRegistry.playC2S().register(
            PacketC2SFormationSettingRequest.payloadID, PacketC2SFormationSettingRequest.CODEC
        )
        PayloadTypeRegistry.playC2S().register(
            PacketC2SFormationSettingChangeRequest.payloadID, PacketC2SFormationSettingChangeRequest.CODEC
        )
        PayloadTypeRegistry.playC2S().register(
            PacketC2SFriendListRequest.payloadID, PacketC2SFriendListRequest.CODEC
        )
        PayloadTypeRegistry.playC2S().register(
            PacketC2SFriendAddRequest.payloadID, PacketC2SFriendAddRequest.CODEC
        )
        PayloadTypeRegistry.playC2S().register(
            PacketC2SFriendRemoveRequest.payloadID, PacketC2SFriendRemoveRequest.CODEC
        )

        PayloadTypeRegistry.playS2C().register(
            PacketS2CManaDataToggle.payloadID, PacketS2CManaDataToggle.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CFriendListResponse.payloadID, PacketS2CFriendListResponse.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CFriendChangeResponse.payloadID, PacketS2CFriendChangeResponse.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CFormationSettingsResponse.payloadID, PacketS2CFormationSettingsResponse.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CEnergyCrystalChange.payloadID, PacketS2CEnergyCrystalChange.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CFormationCreate.payloadID, PacketS2CFormationCreate.CODEC
        )
        PayloadTypeRegistry.playS2C().register(
            PacketS2CFormationBreak.payloadID, PacketS2CFormationBreak.CODEC
        )
    }

    private fun loadPacketListener() {
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendListRequest.payloadID
        ) { packet, ctx ->
            FriendListRequestHandler.receive(packet, FabricServerContext(ctx))
        }
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendAddRequest.payloadID
        ) { packet, ctx ->
            FriendAddListRequestHandler.receive(packet, FabricServerContext(ctx))
        }

        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendRemoveRequest.payloadID
        ) { packet, ctx ->
            FriendRemoveListRequestHandler.receive(packet, FabricServerContext(ctx))
        }

        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFormationSettingRequest.payloadID
        ) { packet, ctx ->
            FormationSettingRequestPacketListener.receive(packet, FabricServerContext(ctx))
        }

        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFormationSettingChangeRequest.payloadID
        ) { packet, ctx ->
            FormationSettingChangePacketListener.receive(packet, FabricServerContext(ctx))
        }
    }

}