package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.cooparticlesapi.platform.network.ClientContext
import cn.coostack.usefulmagic.extend.asHolder
import cn.coostack.usefulmagic.packet.s2c.PacketS2CTrackerToggle

object TrackerToggleListener {
    fun receive(
        payload: PacketS2CTrackerToggle,
        context: ClientContext
    ) {
        val id = payload.targetID
        val player = context.client().player ?: return
        val level = player.clientLevel
        val entity = level.getEntity(id) ?: player.takeIf { it.id == id } ?: return
        // 通过ID 直接设置他的值， 然后try apply
        val holder = entity.asHolder()
        holder.getCooTracker().applyChange(payload.tracker)
    }
}
