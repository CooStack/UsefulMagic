package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * 这里包括了添加和删除
 */
class PacketS2CFriendChangeResponse(val owner: UUID, val status: Boolean) : CustomPayload {
    companion object {
        val payloadID = CustomPayload.Id<PacketS2CFriendChangeResponse>(
            Identifier.of(UsefulMagic.MOD_ID, "friend_change_response")
        )

        val CODEC: PacketCodec<PacketByteBuf, PacketS2CFriendChangeResponse> =
            CustomPayload.codecOf<PacketByteBuf, PacketS2CFriendChangeResponse>(
                { data, buf ->
                    buf.writeUuid(data.owner)
                    buf.writeBoolean(data.status)
                }, {
                    val owner = it.readUuid()
                    val status = it.readBoolean()
                    PacketS2CFriendChangeResponse(owner, status)
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