package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * 回信
 */
class PacketS2CFriendListResponse(val friends: List<PlayerProfile>, val maxPage: Int, val owner: UUID) : CustomPayload {
    data class PlayerProfile(val uuid: UUID, val name: String)
    companion object {
        val payloadID = CustomPayload.Id<PacketS2CFriendListResponse>(
            Identifier.of(UsefulMagic.MOD_ID, "friend_list_response")
        )

        val CODEC = CustomPayload.codecOf<PacketByteBuf, PacketS2CFriendListResponse>(
            { data, buf ->
                buf.writeUuid(data.owner)
                buf.writeInt(data.maxPage)
                buf.writeInt(data.friends.size)
                data.friends.forEach {
                    buf.writeUuid(it.uuid)
                    buf.writeString(it.name)
                }
            }, {
                val owner = it.readUuid()
                val page = it.readInt()
                val size = it.readInt()
                val friends = mutableListOf<PlayerProfile>()
                repeat(size) { index ->
                    friends.add(PlayerProfile(it.readUuid(), it.readString()))
                }
                PacketS2CFriendListResponse(friends, page, owner)
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