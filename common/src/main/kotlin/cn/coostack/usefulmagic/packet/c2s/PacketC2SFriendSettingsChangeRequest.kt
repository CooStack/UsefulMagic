package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * FriendManager friendly settings change request.
 */
class PacketC2SFriendSettingsChangeRequest(
    val owner: UUID,
    val treatHostileAsFriend: Boolean,
    val treatNeutralAsFriend: Boolean,
    val treatNonFriendPlayerAsFriend: Boolean,
    val treatFriendPlayerAsFriend: Boolean,
    val treatAnimalAsFriend: Boolean,
    val treatFriendlyMobAsFriend: Boolean
) : CustomPacketPayload {

    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFriendSettingsChangeRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFriendSettingsChangeRequest>(
                { packet, buf ->
                    buf.writeUUID(packet.owner)
                    buf.writeBoolean(packet.treatHostileAsFriend)
                    buf.writeBoolean(packet.treatNeutralAsFriend)
                    buf.writeBoolean(packet.treatNonFriendPlayerAsFriend)
                    buf.writeBoolean(packet.treatFriendPlayerAsFriend)
                    buf.writeBoolean(packet.treatAnimalAsFriend)
                    buf.writeBoolean(packet.treatFriendlyMobAsFriend)
                },
                { buf ->
                    PacketC2SFriendSettingsChangeRequest(
                        owner = buf.readUUID(),
                        treatHostileAsFriend = buf.readBoolean(),
                        treatNeutralAsFriend = buf.readBoolean(),
                        treatNonFriendPlayerAsFriend = buf.readBoolean(),
                        treatFriendPlayerAsFriend = buf.readBoolean(),
                        treatAnimalAsFriend = buf.readBoolean(),
                        treatFriendlyMobAsFriend = buf.readBoolean()
                    )
                }
            )

        val payloadID = CustomPacketPayload.Type<PacketC2SFriendSettingsChangeRequest>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "friend_settings_change_request")
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}
