package cn.coostack.usefulmagic.packet.listener

import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataResponse
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ManaRequestListener: ClientPlayNetworking.PlayPayloadHandler<PacketS2CManaDataResponse> {
    override fun receive(
        payload: PacketS2CManaDataResponse,
        context: ClientPlayNetworking.Context
    ) {
        // 设定参数


    }
}