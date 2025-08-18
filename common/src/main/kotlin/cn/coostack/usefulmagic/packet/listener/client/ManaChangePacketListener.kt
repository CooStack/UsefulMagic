package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.cooparticlesapi.platform.network.ClientContext
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle

object ManaChangePacketListener {
    fun receive(
        packet: PacketS2CManaDataToggle,
        context: ClientContext
    ) {
        ClientManaManager.receiveChange(packet.who, packet.data)
    }
}