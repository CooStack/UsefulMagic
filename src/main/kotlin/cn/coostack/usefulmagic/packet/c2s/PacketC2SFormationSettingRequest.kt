package cn.coostack.usefulmagic.packet.c2s

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class PacketC2SFormationSettingRequest(val clickPos: BlockPos) : CustomPayload {
    companion object {
        val CODEC: PacketCodec<PacketByteBuf, PacketC2SFormationSettingRequest> =
            CustomPayload.codecOf<PacketByteBuf, PacketC2SFormationSettingRequest>(
                { p, b ->
                    b.writeVec3d(p.clickPos.toCenterPos())
                }, {
                    return@codecOf PacketC2SFormationSettingRequest(BlockPos.ofFloored(it.readVec3d()))
                }
            )

        val payloadID =
            CustomPayload.Id<PacketC2SFormationSettingRequest>(
                Identifier.of(UsefulMagic.MOD_ID, "formation_settings_request")
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