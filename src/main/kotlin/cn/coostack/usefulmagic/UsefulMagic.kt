package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.beans.PlayerManaData
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entitiy.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.listener.DefendMagicListener
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockRenderer
import cn.coostack.usefulmagic.meteorite.MeteoriteManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SManaDataRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.states.ManaServerState
import cn.coostack.usefulmagic.utils.ComboUtil
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
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
    }


    private fun loadRecipeTypes() {
        UsefulMagicRecipeTypes.register()
    }

    private fun loadListeners() {

        DefendMagicListener.init()
    }

    private fun loadTickers() {
        ServerTickEvents.START_SERVER_TICK.register { s ->
            state.playerManaData.values.forEach(PlayerManaData::tick)
            state.sendToggle()
            MeteoriteManager.doTick()
            ComboUtil.tick()
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
        PacketS2CManaDataResponse.init()
        PacketC2SManaDataRequest.init()
        logger.debug("数据包注册完成")
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