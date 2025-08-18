package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.cooparticlesapi.extend.ofFloored
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 回信
 */
class PacketS2CFriendListResponse(val friends: List<PlayerProfile>, val maxPage: Int, val owner: UUID) : CustomPacketPayload {
    data class PlayerProfile(val uuid: UUID, val name: String)
    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CFriendListResponse>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_list_response")
        )

        val CODEC = CustomPacketPayload.codec<FriendlyByteBuf, PacketS2CFriendListResponse>(
            { data, buf->
                buf.writeUUID(data.owner)
                buf.writeInt(data.maxPage)
                buf.writeInt(data.friends.size)
                data.friends.forEach {
                    buf.writeUUID(it.uuid)
                    buf.writeUtf(it.name)
                }
            }, {
                val owner = it.readUUID()
                val page = it.readInt()
                val size = it.readInt()
                val friends = mutableListOf<PlayerProfile>()
                repeat(size) { index ->
                    friends.add(PlayerProfile(it.readUUID(), it.readUtf()))
                }
                PacketS2CFriendListResponse(friends, page, owner)
            }
        )


    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}