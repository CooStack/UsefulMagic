package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationSettings
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/**
 * @param settings 返回的设置内容
 * @param isOwner 是否是阵法的主人 (null或者uuid不和发送者相同均设置为false)
 */
class PacketS2CFormationSettingsResponse(val settings: FormationSettings, val isOwner: Boolean) :
    CustomPayload {
    companion object {
        val payloadID = CustomPayload.Id<PacketS2CFormationSettingsResponse>(
            Identifier.of(UsefulMagic.MOD_ID, "formation_settings_response")
        )

        val CODEC: PacketCodec<PacketByteBuf, PacketS2CFormationSettingsResponse> =
            CustomPayload.codecOf<PacketByteBuf, PacketS2CFormationSettingsResponse>(
                { data, buf ->
                    val settings = data.settings
                    buf.writeBoolean(settings.hostileEntityAttack)
                    buf.writeBoolean(settings.animalEntityAttack)
                    buf.writeBoolean(settings.playerEntityAttack)
                    buf.writeBoolean(settings.anotherEntityAttack)
                    buf.writeBoolean(settings.displayParticleOnlyTrigger)
                    buf.writeDouble(settings.triggerRange)
                    buf.writeBoolean(data.isOwner)
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
                    PacketS2CFormationSettingsResponse(settings, it.readBoolean())
                }
            )

        fun init() {
            PayloadTypeRegistry.playS2C().register(payloadID, CODEC)
        }

    }

    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return payloadID
    }
}