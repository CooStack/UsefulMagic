package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.beans.MagicPlayerData
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.listener.DefendMagicListener
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
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
import cn.coostack.usefulmagic.states.ManaServerState
import cn.coostack.usefulmagic.utils.ComboUtil
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory

object UsefulMagic : ModInitializer {
    val logger = LoggerFactory.getLogger("UsefulMagic")
    const val MOD_ID = "usefulmagic"
    lateinit var server: MinecraftServer
    lateinit var state: ManaServerState
    override fun onInitialize() {
        registerBlocks()
        registerItems()
        loadPacket()
        loadTickers()
        loadFuel()
        loadBlockEntities()
        loadRecipeTypes()
        loadListeners()
        loadSounds()
        loadEntityAttributes()
        loadPacketListener()
    }


    private fun loadRecipeTypes() {
        UsefulMagicRecipeTypes.register()
    }

    private fun loadListeners() {
        DefendMagicListener.init()
    }

    private fun loadTickers() {
        ServerTickEvents.START_SERVER_TICK.register { s ->
            state.magicPlayerData.values.forEach(MagicPlayerData::tick)
            state.sendToggle()
            MeteoriteManager.doTick()
            ComboUtil.tick()
            ServerFormationManager.removeNotActiveFormations()
            MagicAxe.postPlayerAxeSkillTick()
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            this.state = ManaServerState.getFromState(server)
            this.server = server
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
        logger.debug("服务器随机刻加载完成")
    }


    private fun loadFuel() {
        FuelRegistry.INSTANCE.add(UsefulMagicItems.WOODEN_WAND, 200)
    }

    private fun registerBlocks() {
        UsefulMagicBlocks.init()
        UsefulMagicBlockEntities.reg()
    }

    private fun registerItems() {
        UsefulMagicDataComponentTypes.init()
        UsefulMagicItems.init()
        UsefulMagicItemGroups.init()
        logger.debug("物品注册完成")
    }

    private fun loadPacket() {
        PacketS2CManaDataToggle.init()
        PacketS2CFriendListResponse.init()
        PacketS2CFriendChangeResponse.init()
        PacketS2CFormationSettingsResponse.init()
        PacketS2CEnergyCrystalChange.init()
        PacketS2CFormationCreate.init()
        PacketS2CFormationBreak.init()
        PacketC2SFormationSettingRequest.init()
        PacketC2SFormationSettingChangeRequest.init()
        PacketC2SFriendListRequest.init()
        PacketC2SFriendAddRequest.init()
        PacketC2SFriendRemoveRequest.init()
        logger.debug("数据包注册完成")
    }

    /**
     * 处理客户端请求, 并发送回信
     */
    private fun loadPacketListener() {
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendListRequest.payloadID, FriendListRequestHandler
        )
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendAddRequest.payloadID, FriendAddListRequestHandler
        )
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFriendRemoveRequest.payloadID, FriendRemoveListRequestHandler
        )
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFormationSettingRequest.payloadID, FormationSettingRequestPacketListener
        )
        ServerPlayNetworking.registerGlobalReceiver(
            PacketC2SFormationSettingChangeRequest.payloadID, FormationSettingChangePacketListener
        )
    }

    private fun loadEntityAttributes() {
        UsefulMagicEntityTypes.init()
        FabricDefaultAttributeRegistry.register(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE,
            MagicBookEntity.createDefaultMobAttributes()
        )
    }

    private fun loadSounds() {
        UsefulMagicSoundEvents.init()
    }

    private fun loadBlockEntities() {
        MeteoriteFallingBlockEntity.init()
    }
}