package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object FormationSettingsPacketResponseListener : ClientPlayNetworking.PlayPayloadHandler<PacketS2CFormationSettingsResponse> {
    override fun receive(
        payload: PacketS2CFormationSettingsResponse,
        context: ClientPlayNetworking.Context
    ) {
        ClientRequestManager.setResponse(payload)
    }
}