package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.cooparticlesapi.extend.ofFloored
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 这里包括了添加和删除
 */
class PacketS2CFriendChangeResponse(val owner: UUID, val status: Boolean) : CustomPacketPayload {
    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CFriendChangeResponse>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_change_response")
        )

        val CODEC: StreamCodec<FriendlyByteBuf, PacketS2CFriendChangeResponse> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketS2CFriendChangeResponse>(
                { data, buf ->
                    buf.writeUUID(data.owner)
                    buf.writeBoolean(data.status)
                }, {
                    val owner = it.readUUID()
                    val status = it.readBoolean()
                    PacketS2CFriendChangeResponse(owner, status)
                }
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}