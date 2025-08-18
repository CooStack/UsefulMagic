package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.MagicPlayerData
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * 服务器同步给单个客户端 魔力值改变信息
 * ClientBound
 */
class PacketS2CManaDataToggle(val data: MagicPlayerData, val who: UUID) : CustomPacketPayload {
    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CManaDataToggle>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "mana_data_toggle")
        )
        val CODEC = CustomPacketPayload.codec<RegistryFriendlyByteBuf, PacketS2CManaDataToggle>({ data, buf ->
            buf.writeInt(data.data.mana)
            buf.writeInt(data.data.maxMana)
            buf.writeInt(data.data.manaRegeneration)
            buf.writeUUID(data.who)
        }, {
            val mana = it.readInt()
            val maxMana = it.readInt()
            val manaRegeneration = it.readInt()
            val uuid = it.readUUID()
            return@codec PacketS2CManaDataToggle(
                MagicPlayerData(uuid).apply {
                    this.maxMana = maxMana
                    this.mana = mana
                    this.manaRegeneration = manaRegeneration
                },
                uuid
            )
        })

    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}