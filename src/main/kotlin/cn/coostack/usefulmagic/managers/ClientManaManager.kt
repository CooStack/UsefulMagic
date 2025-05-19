package cn.coostack.usefulmagic.managers

import cn.coostack.usefulmagic.beans.PlayerManaData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

@Environment(EnvType.CLIENT)
object ClientManaManager {


    val data by lazy {
        val data = PlayerManaData(MinecraftClient.getInstance().player!!.uuid)
        data
    }

    fun getSelfMana(): PlayerManaData {
        return data
    }

    fun receiveChange(who: UUID, data: PlayerManaData) {
        val origin = this.data
        if (who != origin.owner) {
            return
        }
        origin.maxMana = data.maxMana
        origin.mana = data.mana
        origin.manaRegeneration = data.manaRegeneration
    }

}