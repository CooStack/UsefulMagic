package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * @param requestUUID 请求该玩家的魔力数据的UUID
 */
class PacketC2SManaDataRequest(val requestUUID: UUID) : CustomPayload {
    companion object {
        val CODEC = CustomPayload.codecOf<RegistryByteBuf, PacketC2SManaDataRequest>(
            { p, b ->
                b.writeUuid(p.requestUUID)
            }, {
                return@codecOf PacketC2SManaDataRequest(it.readUuid())
            }
        )

        val payloadID =
            CustomPayload.Id<PacketC2SManaDataRequest>(
                Identifier.of(UsefulMagic.MOD_ID, "mana_data_request")
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