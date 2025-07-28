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
 * @param requestUUID 请求该玩家的数据的UUID
 * @param page 请求分页 目前设定10个数据为1页
 *
 * 发送后, 服务器会发回提交给客户端 (回调)
 */
class PacketC2SFriendListRequest(val requestUUID: UUID, val page: Int) : CustomPayload {
    companion object {
        val CODEC: PacketCodec<PacketByteBuf, PacketC2SFriendListRequest> =
            CustomPayload.codecOf<PacketByteBuf, PacketC2SFriendListRequest>(
                { p, b ->
                    b.writeUuid(p.requestUUID)
                    b.writeInt(p.page)
                }, {
                    return@codecOf PacketC2SFriendListRequest(it.readUuid(), it.readInt())
                }
            )

        val payloadID =
            CustomPayload.Id<PacketC2SFriendListRequest>(
                Identifier.of(UsefulMagic.MOD_ID, "friend_list_request")
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