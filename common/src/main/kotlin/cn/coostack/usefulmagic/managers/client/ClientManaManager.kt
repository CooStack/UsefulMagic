package cn.coostack.usefulmagic.managers.client

import cn.coostack.usefulmagic.beans.MagicPlayerData
import net.minecraft.client.Minecraft
import java.util.UUID

object ClientManaManager {
    val data by lazy {
        val data = MagicPlayerData(Minecraft.getInstance().player!!.uuid)
        data
    }

    fun getSelfMana(): MagicPlayerData {
        return data
    }

    fun receiveChange(who: UUID, data: MagicPlayerData) {
        val origin = this.data
        if (who != origin.owner) {
            return
        }
        origin.maxMana = data.maxMana
        origin.mana = data.mana
        origin.manaRegeneration = data.manaRegeneration
    }

}