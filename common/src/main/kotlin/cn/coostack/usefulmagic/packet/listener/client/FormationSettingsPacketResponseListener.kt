package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.cooparticlesapi.platform.network.ClientContext
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse

object FormationSettingsPacketResponseListener {
    fun receive(
        payload: PacketS2CFormationSettingsResponse,
        context: ClientContext
    ) {
        ClientRequestManager.setResponse(payload)
    }
}