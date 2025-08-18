package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class PacketC2SFormationSettingRequest(val clickPos: BlockPos) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, PacketC2SFormationSettingRequest> =
            CustomPacketPayload.codec<FriendlyByteBuf, PacketC2SFormationSettingRequest>(
                { p, b ->
                    b.writeVec3(p.clickPos.center)
                }, {
                    return@codec PacketC2SFormationSettingRequest(ofFloored(it.readVec3()))
                }
            )

        val payloadID =
            CustomPacketPayload.Type<PacketC2SFormationSettingRequest>(
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "formation_settings_request")
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}