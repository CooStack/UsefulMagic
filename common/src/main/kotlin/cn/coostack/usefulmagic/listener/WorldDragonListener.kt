package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.annotations.events.EventHandler
import cn.coostack.cooparticlesapi.annotations.events.EventListener
import cn.coostack.cooparticlesapi.event.events.world.server.ServerWorldPostTickEvent
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.DragonSpawner

@EventListener(UsefulMagic.MOD_ID)
object WorldDragonListener {
    @EventHandler
    fun onWorldTickServer(event: ServerWorldPostTickEvent) {
        DragonSpawner.tick(event.world)
    }
}