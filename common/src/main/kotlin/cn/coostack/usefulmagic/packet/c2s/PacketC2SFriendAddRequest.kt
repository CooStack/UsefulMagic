package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 增加好友请求 - 无 response
 */
class PacketC2SFriendAddRequest(val owner: UUID, val target: UUID) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFriendAddRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFriendAddRequest>(
                { p, b ->
                    b.writeUUID(p.owner)
                    b.writeUUID(p.target)
                }, {
                    return@codec PacketC2SFriendAddRequest(it.readUUID(), it.readUUID())
                }
            )

        val payloadID =
            CustomPacketPayload.Type<PacketC2SFriendAddRequest>(
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_add_request")
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}