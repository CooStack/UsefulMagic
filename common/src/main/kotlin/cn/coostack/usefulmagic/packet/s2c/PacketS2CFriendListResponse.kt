package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 回信
 */
class PacketS2CFriendListResponse(
    val friends: List<PlayerProfile>,
    val maxPage: Int,
    val owner: UUID,
    val treatHostileAsFriend: Boolean,
    val treatNeutralAsFriend: Boolean,
    val treatNonFriendPlayerAsFriend: Boolean,
    val treatFriendPlayerAsFriend: Boolean,
    val treatAnimalAsFriend: Boolean,
    val treatFriendlyMobAsFriend: Boolean
) : CustomPacketPayload {
    data class PlayerProfile(val uuid: UUID, val name: String)

    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CFriendListResponse>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_list_response")
        )

        val CODEC = CustomPacketPayload.codec<FriendlyByteBuf, PacketS2CFriendListResponse>(
            { data, buf ->
                buf.writeUUID(data.owner)
                buf.writeInt(data.maxPage)
                buf.writeBoolean(data.treatHostileAsFriend)
                buf.writeBoolean(data.treatNeutralAsFriend)
                buf.writeBoolean(data.treatNonFriendPlayerAsFriend)
                buf.writeBoolean(data.treatFriendPlayerAsFriend)
                buf.writeBoolean(data.treatAnimalAsFriend)
                buf.writeBoolean(data.treatFriendlyMobAsFriend)
                buf.writeInt(data.friends.size)
                data.friends.forEach {
                    buf.writeUUID(it.uuid)
                    buf.writeUtf(it.name)
                }
            }, { buf ->
                val owner = buf.readUUID()
                val page = buf.readInt()
                val hostile = buf.readBoolean()
                val neutral = buf.readBoolean()
                val nonFriendPlayer = buf.readBoolean()
                val friendPlayer = buf.readBoolean()
                val animal = buf.readBoolean()
                val friendlyMob = buf.readBoolean()
                val size = buf.readInt()
                val friends = mutableListOf<PlayerProfile>()
                repeat(size) {
                    friends.add(PlayerProfile(buf.readUUID(), buf.readUtf()))
                }
                PacketS2CFriendListResponse(
                    friends = friends,
                    maxPage = page,
                    owner = owner,
                    treatHostileAsFriend = hostile,
                    treatNeutralAsFriend = neutral,
                    treatNonFriendPlayerAsFriend = nonFriendPlayer,
                    treatFriendPlayerAsFriend = friendPlayer,
                    treatAnimalAsFriend = animal,
                    treatFriendlyMobAsFriend = friendlyMob
                )
            }
        )


    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}
