package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/**
 * 阵法被破坏时发送 (告知客户端这里爆炸了)
 */
class PacketS2CFormationBreak(val formationPos: BlockPos, val damage: Float) : CustomPayload {
    companion object {

        val payloadID = CustomPayload.Id<PacketS2CFormationBreak>(Identifier.of(UsefulMagic.MOD_ID, "formation_break"))

        val CODEC = PacketCodec.of<PacketByteBuf, PacketS2CFormationBreak>(
            { data, buf ->
                buf.writeVec3d(data.formationPos.toCenterPos())
                buf.writeFloat(data.damage)
            }, { buf ->
                PacketS2CFormationBreak(BlockPos.ofFloored(buf.readVec3d()), buf.readFloat())
            }
        )!!

        fun init() {
            PayloadTypeRegistry.playS2C().register(payloadID, CODEC)
        }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return payloadID
    }
}