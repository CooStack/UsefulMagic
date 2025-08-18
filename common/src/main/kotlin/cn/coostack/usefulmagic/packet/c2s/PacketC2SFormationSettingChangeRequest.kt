package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationSettings
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class PacketC2SFormationSettingChangeRequest(val clickPos: BlockPos, val changed: FormationSettings) :
    CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFormationSettingChangeRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFormationSettingChangeRequest>(
                { p, buf ->
                    val settings = p.changed
                    buf.writeBoolean(settings.hostileEntityAttack)
                    buf.writeBoolean(settings.animalEntityAttack)
                    buf.writeBoolean(settings.playerEntityAttack)
                    buf.writeBoolean(settings.anotherEntityAttack)
                    buf.writeBoolean(settings.displayParticleOnlyTrigger)
                    buf.writeDouble(settings.triggerRange)
                    buf.writeVec3(p.clickPos.center)
                }, {
                    val settings = FormationSettings()
                    settings.apply {
                        hostileEntityAttack = it.readBoolean()
                        animalEntityAttack = it.readBoolean()
                        playerEntityAttack = it.readBoolean()
                        anotherEntityAttack = it.readBoolean()
                        displayParticleOnlyTrigger = it.readBoolean()
                        triggerRange = it.readDouble()
                    }
                    return@codec PacketC2SFormationSettingChangeRequest(ofFloored(it.readVec3()), settings)
                }
            )

        val payloadID =
            CustomPacketPayload.Type<PacketC2SFormationSettingChangeRequest>(
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "formation_settings_change_request")
            )

    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }

}