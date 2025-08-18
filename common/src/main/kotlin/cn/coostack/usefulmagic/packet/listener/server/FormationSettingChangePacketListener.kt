package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse

object FormationSettingChangePacketListener {
    fun receive(
        payload: PacketC2SFormationSettingChangeRequest,
        context: ServerContext
    ) {
        val player = context.player() // 发出请求的玩家
        val pos = payload.clickPos
        val world = player.level()
        val formationEntity = world.getBlockEntity(pos)
        val isOwner = if (formationEntity !is FormationCoreBlockEntity) {
            false
        } else formationEntity.formation.owner == player.uuid
        if (!isOwner) {
            context.reply(PacketS2CFormationSettingsResponse(FormationSettings(), false))
            return
        }
        formationEntity as FormationCoreBlockEntity
        val changed = payload.changed
        formationEntity.formation.settings.apply {
            this.playerEntityAttack = changed.playerEntityAttack
            this.animalEntityAttack = changed.animalEntityAttack
            this.anotherEntityAttack = changed.anotherEntityAttack
            this.hostileEntityAttack = changed.hostileEntityAttack
            this.displayParticleOnlyTrigger = changed.displayParticleOnlyTrigger
            if (formationEntity.formation.getFormationTriggerRange() < changed.triggerRange) {
                this.triggerRange = -1.0
                return@apply
            }
            if (changed.triggerRange <= 0 && changed.triggerRange != -1.0) {
                this.triggerRange = -1.0
                return@apply
            }
            this.triggerRange = changed.triggerRange
        }
        context.reply(
            PacketS2CFormationSettingsResponse(formationEntity.formation.settings, true)
        )

    }

}