package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * 阵法被破坏时发送 (告知客户端这里爆炸了)
 */
class PacketS2CEnergyCrystalChange(val crystal: BlockPos, val mana: Int, val maxMana: Int) : CustomPacketPayload {
    companion object {

        val payloadID =
            CustomPacketPayload.Type<PacketS2CEnergyCrystalChange>(
                ResourceLocation.fromNamespaceAndPath(
                    UsefulMagic.MOD_ID,
                    "energy_crystal_change"
                )
            )

        val CODEC = StreamCodec.of<FriendlyByteBuf, PacketS2CEnergyCrystalChange>(
            { buf, data ->
                buf.writeVec3(data.crystal.center)
                buf.writeInt(data.mana)
                buf.writeInt(data.maxMana)
            }, { buf ->
                PacketS2CEnergyCrystalChange(ofFloored(buf.readVec3()), buf.readInt(), buf.readInt())
            }
        )
        
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return payloadID
    }
}