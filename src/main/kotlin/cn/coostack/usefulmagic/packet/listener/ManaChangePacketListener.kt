package cn.coostack.usefulmagic.packet.listener

import cn.coostack.usefulmagic.managers.ClientManaManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ManaChangePacketListener : ClientPlayNetworking.PlayPayloadHandler<PacketS2CManaDataToggle> {
    override fun receive(
        packet: PacketS2CManaDataToggle,
        context: ClientPlayNetworking.Context
    ) {
        ClientManaManager.receiveChange(packet.who, packet.data)
    }
}