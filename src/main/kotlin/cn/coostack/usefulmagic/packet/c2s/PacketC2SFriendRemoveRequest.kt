package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * 移除好友请求 - 无 response
 */
class PacketC2SFriendRemoveRequest(val owner: UUID, val target: UUID) : CustomPayload {
    companion object {
        val CODEC: PacketCodec<PacketByteBuf, PacketC2SFriendRemoveRequest> =
            CustomPayload.codecOf<PacketByteBuf, PacketC2SFriendRemoveRequest>(
                { p, b ->
                    b.writeUuid(p.owner)
                    b.writeUuid(p.target)
                }, {
                    return@codecOf PacketC2SFriendRemoveRequest(it.readUuid(), it.readUuid())
                }
            )

        val payloadID =
            CustomPayload.Id<PacketC2SFriendRemoveRequest>(
                Identifier.of(UsefulMagic.MOD_ID, "friend_remove_request")
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