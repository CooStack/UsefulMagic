package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendAddRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse


/**
 * 收到来自客户端的请求后, 发回信息
 */
object FriendRemoveListRequestHandler  {
    fun receive(
        payload: PacketC2SFriendRemoveRequest,
        context: ServerContext
    ) {
        val player = context.player() // 发出请求的玩家
        val who = payload.target
        val data = UsefulMagic.state.getDataFromServer(player.uuid)
        data.friends.remove(who)

        val response = PacketS2CFriendChangeResponse(payload.owner, true)
         context.reply(response)
    }

}