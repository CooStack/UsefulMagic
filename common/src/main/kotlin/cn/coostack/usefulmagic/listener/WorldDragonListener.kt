package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.annotations.events.EventHandler
import cn.coostack.cooparticlesapi.annotations.events.EventListener
import cn.coostack.cooparticlesapi.event.events.server.ServerPostTickEvent
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.util.DragonSpawner

@EventListener(UsefulMagic.MOD_ID)
object WorldDragonListener {
    @EventHandler
    fun onWorldTickServer(event: ServerPostTickEvent) {
        DragonSpawner.tick()
    }
}