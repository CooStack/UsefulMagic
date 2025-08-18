package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.minecraft.server.level.ServerPlayer


/**
 * 收到来自客户端的请求后, 发回信息
 */
object FriendListRequestHandler {
    fun receive(
        payload: PacketC2SFriendListRequest,
        context: ServerContext
    ) {
        val player = context.player() // 发出请求的玩家
        val requestUUID = payload.requestUUID
        val page = payload.page
        val start = (page - 1) * 10
        val end = page * 10
        val state = UsefulMagic.state.getDataFromServer(requestUUID)
        val stateFriends = state.friends
        val responseFriends = if (stateFriends.isEmpty()) {
            emptyList()
        } else {
            val res = arrayListOf<PacketS2CFriendListResponse.PlayerProfile>()
            for (i in start..<end.coerceAtMost(stateFriends.size)) {
                val uuid = stateFriends[i]
                val name = UsefulMagic.server.playerList.getPlayer(uuid)?.name
                res.add(PacketS2CFriendListResponse.PlayerProfile(uuid, name?.string ?: "the player name not found"))
            }
            res
        }
        val packet = PacketS2CFriendListResponse(
            responseFriends,
            stateFriends.size / 10 + 1,
            requestUUID
        )
        CooParticlesServices.SERVER_NETWORK.send(
            packet,
            player as ServerPlayer
        )
    }

}