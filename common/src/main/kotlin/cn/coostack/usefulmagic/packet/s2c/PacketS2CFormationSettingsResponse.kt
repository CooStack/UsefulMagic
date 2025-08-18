package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.cooparticlesapi.extend.ofFloored
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.BlockPos

/**
 * @param settings 返回的设置内容
 * @param isOwner 是否是阵法的主人 (null或者uuid不和发送者相同均设置为false)
 */
class PacketS2CFormationSettingsResponse(val settings: FormationSettings, val isOwner: Boolean) :
    CustomPacketPayload {
    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CFormationSettingsResponse>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "formation_settings_response")
        )

        val CODEC: StreamCodec<FriendlyByteBuf, PacketS2CFormationSettingsResponse> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketS2CFormationSettingsResponse>(
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

    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}