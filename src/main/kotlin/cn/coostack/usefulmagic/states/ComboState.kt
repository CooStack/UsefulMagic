package cn.coostack.usefulmagic.states

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

class ComboState(val owner: UUID) {
    var count = 0

    /**
     * combo有效时长
     * 超过了这自动设置为0
     */
    var validComboTime = 0

    var defaultComboTime = 20 * 60

    fun increase() {
        count++
        validComboTime = defaultComboTime
    }

    fun increase(comboTime: Int) {
        count++
        validComboTime = comboTime
    }

    fun reset() {
        count = 0
        validComboTime = 0
    }


    /**
     * @param predicate 返回true则自增
     */
    fun increaseIf(predicate: (owner: PlayerEntity) -> Boolean) {
        val player = UsefulMagic.server.playerManager.getPlayer(owner) ?: return
        if (predicate(player)) {
            increase()
        }
    }

    /**
     * @param predicate 返回true则自增
     * @param comboTime 连击有效时间 tick
     */
    fun increaseIf(comboTime: Int, predicate: (owner: PlayerEntity) -> Boolean) {
        val player = UsefulMagic.server.playerManager.getPlayer(owner) ?: return
        if (predicate(player)) {
            increase(comboTime)
        }
    }

    fun comboTick() {
        if (validComboTime-- <= 0) {
            reset()
            validComboTime = 0
        }
    }

}