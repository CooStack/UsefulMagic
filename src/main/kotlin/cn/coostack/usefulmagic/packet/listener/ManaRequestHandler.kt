package cn.coostack.usefulmagic.packet.listener

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.packet.c2s.PacketC2SManaDataRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataResponse
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object ManaRequestHandler : ServerPlayNetworking.PlayPayloadHandler<PacketC2SManaDataRequest> {
    override fun receive(
        payload: PacketC2SManaDataRequest,
        context: ServerPlayNetworking.Context
    ) {
        val player = context.player() // 发出请求的玩家
        val requestUUID = payload.requestUUID

        val res = UsefulMagic.state.getDataFromServer(requestUUID)

        ServerPlayNetworking.send(
            player,
            PacketS2CManaDataResponse(res)
        )
    }

}