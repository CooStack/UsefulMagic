package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse

object FormationSettingRequestPacketListener {
    fun receive(
        payload: PacketC2SFormationSettingRequest,
        context: ServerContext,
    ) {
        val player = context.player() // 发出请求的玩家
        val pos = payload.clickPos
        val world = player.level()
        val formationEntity = world.getBlockEntity(pos)
        val isOwner = if (formationEntity !is FormationCoreBlockEntity) {
            false
        } else formationEntity.formation.owner == player.uuid
        if (!isOwner) {
            context.reply(
                PacketS2CFormationSettingsResponse(FormationSettings(), false)
            )
            return
        }
        formationEntity as FormationCoreBlockEntity

        context.reply(
            PacketS2CFormationSettingsResponse(formationEntity.formation.settings, true)
        )

    }

}