package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationSettings
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class PacketC2SFormationSettingChangeRequest(val clickPos: BlockPos, val changed: FormationSettings) : CustomPayload {
    companion object {
        val CODEC: PacketCodec<PacketByteBuf, PacketC2SFormationSettingChangeRequest> =
            CustomPayload.codecOf<PacketByteBuf, PacketC2SFormationSettingChangeRequest>(
                { p, buf ->
                    val settings = p.changed
                    buf.writeBoolean(settings.hostileEntityAttack)
                    buf.writeBoolean(settings.animalEntityAttack)
                    buf.writeBoolean(settings.playerEntityAttack)
                    buf.writeBoolean(settings.anotherEntityAttack)
                    buf.writeBoolean(settings.displayParticleOnlyTrigger)
                    buf.writeDouble(settings.triggerRange)
                    buf.writeVec3d(p.clickPos.toCenterPos())
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
                    return@codecOf PacketC2SFormationSettingChangeRequest(BlockPos.ofFloored(it.readVec3d()), settings)
                }
            )

        val payloadID =
            CustomPayload.Id<PacketC2SFormationSettingChangeRequest>(
                Identifier.of(UsefulMagic.MOD_ID, "formation_settings_change_request")
            )

        fun init() {
            PayloadTypeRegistry.playC2S().register(
                payloadID, CODEC
            )
        }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return payloadID
    }
}