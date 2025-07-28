package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object FriendChangeResponsePacketListener : ClientPlayNetworking.PlayPayloadHandler<PacketS2CFriendChangeResponse> {
    override fun receive(
        payload: PacketS2CFriendChangeResponse,
        context: ClientPlayNetworking.Context
    ) {
        ClientRequestManager.setResponse(payload)
    }
}