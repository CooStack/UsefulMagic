package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.cooparticlesapi.platform.network.ClientContext
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse

object FriendResponsePacketListener {
    fun receive(
        payload: PacketS2CFriendListResponse,
        context: ClientContext
    ) {
        ClientRequestManager.setResponse(payload)
    }
}