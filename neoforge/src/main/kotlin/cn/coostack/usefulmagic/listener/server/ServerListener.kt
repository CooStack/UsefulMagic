package cn.coostack.usefulmagic.listener.server

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.platform.FuelHelper
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent


@EventBusSubscriber(modid = UsefulMagic.MOD_ID)
object ServerListener {
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Pre) {
        UsefulMagic.tickServer()
    }

    @SubscribeEvent
    fun onServerStart(event: ServerStartedEvent) {
        UsefulMagic.setupServer(event.server)
    }

    @SubscribeEvent
    fun onFuel(e: FurnaceFuelBurnTimeEvent) {
        val stack = e.itemStack

        val burn = FuelHelper.fuels.filter {
            stack.isOf(it.key.getItem())
        }.entries.firstOrNull()?.value ?: 0
        if (burn != 0) {
            e.burnTime = burn
        }
    }


}