package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.PlayerManaData
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class PacketS2CManaDataResponse(val data: PlayerManaData) : CustomPayload {
    companion object {
        val payloadID = CustomPayload.Id<PacketS2CManaDataResponse>(
            Identifier.of(UsefulMagic.MOD_ID, "mana_data_response")
        )

        val CODEC = CustomPayload.codecOf<RegistryByteBuf, PacketS2CManaDataResponse>(
            { data, buf ->
                buf.writeInt(data.data.mana)
                buf.writeInt(data.data.maxMana)
                buf.writeInt(data.data.manaRegeneration)
                buf.writeUuid(data.data.owner)
            }, {
                val mana = it.readInt()
                val maxMana = it.readInt()
                val manaRegeneration = it.readInt()
                val owner = it.readUuid()
                val data = PlayerManaData(owner)
                data.apply {
                    this.maxMana = maxMana
                    this.manaRegeneration = manaRegeneration
                    this.mana = mana
                }
                PacketS2CManaDataResponse(data)
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