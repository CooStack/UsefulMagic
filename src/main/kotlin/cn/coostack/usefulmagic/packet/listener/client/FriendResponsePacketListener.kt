package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object FriendResponsePacketListener : ClientPlayNetworking.PlayPayloadHandler<PacketS2CFriendListResponse> {
    override fun receive(
        payload: PacketS2CFriendListResponse,
        context: ClientPlayNetworking.Context
    ) {
        ClientRequestManager.setResponse(payload)
    }
}