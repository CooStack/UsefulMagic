package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * @param requestUUID 请求该玩家的数据的UUID
 * @param page 请求分页 目前设定10个数据为1页
 *
 * 发送后, 服务器会发回提交给客户端 (回调)
 */
class PacketC2SFriendListRequest(val requestUUID: UUID, val page: Int) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFriendListRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFriendListRequest>(
                { p, b ->
                    b.writeUUID(p.requestUUID)
                    b.writeInt(p.page)
                }, {
                    return@codec PacketC2SFriendListRequest(it.readUUID(), it.readInt())
                }
            )

        val payloadID =
            CustomPacketPayload.Type<PacketC2SFriendListRequest>(
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_list_request")
            )

    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}