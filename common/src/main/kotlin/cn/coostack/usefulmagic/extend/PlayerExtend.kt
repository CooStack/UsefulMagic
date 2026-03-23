package cn.coostack.usefulmagic.extend

import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import net.minecraft.world.entity.player.Player

var Player.mana: Int
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.CURRENT_MANA, 100)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.CURRENT_MANA, value)

var Player.maxMana: Int
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.MAX_MANA, 100)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.MAX_MANA, value)

var Player.manaAbsorptionRate: Int
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.MANA_ABSORPTION_RATE, 1)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.MANA_ABSORPTION_RATE, value)

fun Player.copyManaDataFrom(other: Player) {
    maxMana = other.maxMana
    manaAbsorptionRate = other.manaAbsorptionRate
    mana = other.mana.coerceAtMost(maxMana)
}

fun Player.isFullMana(): Boolean = mana >= maxMana
