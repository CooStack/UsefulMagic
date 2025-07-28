package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/**
 * 阵法创建时(包括重新加载) 发包
 */
class PacketS2CFormationCreate(val pos: BlockPos) : CustomPayload {
    companion object {
        val payloadID =
            CustomPayload.Id<PacketS2CFormationCreate>(Identifier.of(UsefulMagic.MOD_ID, "formation_create"))

        val CODEC = PacketCodec.of<PacketByteBuf, PacketS2CFormationCreate>(
            { data, buf ->
                buf.writeVec3d(data.pos.toCenterPos())
            }, {
                PacketS2CFormationCreate(BlockPos.ofFloored(it.readVec3d()))
            }
        )!!

        fun init() {
            PayloadTypeRegistry.playS2C().register(payloadID, CODEC)
        }

    }

    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return payloadID
    }
}