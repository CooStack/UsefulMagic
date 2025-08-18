package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.states.ComboState
import java.util.UUID

/**
 * 玩家连击工具
 */
object ComboUtil {
    val comboPlayers = HashMap<UUID, ComboState>()

    fun getComboState(who: UUID): ComboState {
        return comboPlayers.getOrPut(who) { ComboState(who) }
    }

    fun tick() {
        comboPlayers.forEach {
            it.value.comboTick()
        }
    }

}