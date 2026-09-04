package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class PacketC2SWandMagicExchangeRequest(
    val bagSlot: Int,
    val magicIndex: Int,
    val offhand: Boolean,
    val selectedSlot: Int
) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SWandMagicExchangeRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SWandMagicExchangeRequest>(
                { packet, buf ->
                    buf.writeVarInt(packet.bagSlot)
                    buf.writeVarInt(packet.magicIndex)
                    buf.writeBoolean(packet.offhand)
                    buf.writeVarInt(packet.selectedSlot)
                },
                { buf ->
                    PacketC2SWandMagicExchangeRequest(
                        bagSlot = buf.readVarInt(),
                        magicIndex = buf.readVarInt(),
                        offhand = buf.readBoolean(),
                        selectedSlot = buf.readVarInt()
                    )
                }
            )

        val payloadID = CustomPacketPayload.Type<PacketC2SWandMagicExchangeRequest>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "wand_magic_exchange_request")
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}
