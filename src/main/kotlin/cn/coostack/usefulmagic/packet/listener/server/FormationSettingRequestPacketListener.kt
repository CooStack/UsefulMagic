package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object FormationSettingRequestPacketListener :
    ServerPlayNetworking.PlayPayloadHandler<PacketC2SFormationSettingRequest> {
    override fun receive(
        payload: PacketC2SFormationSettingRequest,
        context: ServerPlayNetworking.Context
    ) {
        val player = context.player() // 发出请求的玩家
        val pos = payload.clickPos
        val world = player.world
        val formationEntity = world.getBlockEntity(pos)
        val isOwner = if (formationEntity !is FormationCoreBlockEntity) {
            false
        } else formationEntity.formation.owner == player.uuid
        if (!isOwner) {
            context.responseSender().sendPacket(
                PacketS2CFormationSettingsResponse(FormationSettings(), false)
            )
            return
        }
        formationEntity as FormationCoreBlockEntity

        context.responseSender().sendPacket(
            PacketS2CFormationSettingsResponse(formationEntity.formation.settings, true)
        )

    }

}