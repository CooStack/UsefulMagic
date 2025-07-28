package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.MagicPlayerData
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.UUID

/**
 * 服务器同步给单个客户端 魔力值改变信息
 */
class PacketS2CManaDataToggle(val data: MagicPlayerData, val who: UUID) : CustomPayload {
    companion object {
        val payloadID = CustomPayload.Id<PacketS2CManaDataToggle>(
            Identifier.of(UsefulMagic.MOD_ID, "mana_data_toggle")
        )
        private val CODEC = CustomPayload.codecOf<RegistryByteBuf, PacketS2CManaDataToggle>({ data, buf ->
            buf.writeInt(data.data.mana)
            buf.writeInt(data.data.maxMana)
            buf.writeInt(data.data.manaRegeneration)
            buf.writeUuid(data.who)
        }, {
            val mana = it.readInt()
            val maxMana = it.readInt()
            val manaRegeneration = it.readInt()
            val uuid = it.readUuid()
            return@codecOf PacketS2CManaDataToggle(
                MagicPlayerData(uuid).apply {
                    this.maxMana = maxMana
                    this.mana = mana
                    this.manaRegeneration = manaRegeneration
                },
                uuid
            )
        })

        fun init() {
            PayloadTypeRegistry.playS2C().register(
                payloadID, CODEC
            )
        }
    }

    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return payloadID
    }
}