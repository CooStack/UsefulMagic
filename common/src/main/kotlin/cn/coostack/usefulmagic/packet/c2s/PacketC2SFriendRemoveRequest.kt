package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 移除好友请求 - 无 response
 */
class PacketC2SFriendRemoveRequest(val owner: UUID, val target: UUID) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFriendRemoveRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFriendRemoveRequest>(
                { p, b ->
                    b.writeUUID(p.owner)
                    b.writeUUID(p.target)
                }, {
                    return@codec PacketC2SFriendRemoveRequest(it.readUUID(), it.readUUID())
                }
            )

        val payloadID =
            CustomPacketPayload.Type<PacketC2SFriendRemoveRequest>(
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_remove_request")
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}