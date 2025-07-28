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
class PacketS2CEnergyCrystalChange(val crystal: BlockPos, val mana: Int, val maxMana: Int) : CustomPayload {
    companion object {

        val payloadID =
            CustomPayload.Id<PacketS2CEnergyCrystalChange>(Identifier.of(UsefulMagic.MOD_ID, "energy_crystal_change"))

        val CODEC = PacketCodec.of<PacketByteBuf, PacketS2CEnergyCrystalChange>(
            { data, buf ->
                buf.writeVec3d(data.crystal.toCenterPos())
                buf.writeInt(data.mana)
                buf.writeInt(data.maxMana)
            }, { buf ->
                PacketS2CEnergyCrystalChange(BlockPos.ofFloored(buf.readVec3d()), buf.readInt(), buf.readInt())
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