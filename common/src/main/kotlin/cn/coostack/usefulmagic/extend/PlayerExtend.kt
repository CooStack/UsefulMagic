package cn.coostack.usefulmagic.extend

import cn.coostack.usefulmagic.data.magic.DiggingState
import cn.coostack.usefulmagic.data.tracked.TrackerManager
import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import net.minecraft.world.entity.Entity
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


var Entity.diggingBlockState: DiggingState?
    get() = this.asHolder().getCooTracker().getOrNull(MagicEntityDataInit.DIGGING_STATE)
    set(value) {
        val tracker = this.asHolder().getCooTracker()
        if (value == null) {
            tracker.remove(MagicEntityDataInit.DIGGING_STATE)
            return
        }
        this.asHolder().getCooTracker().set(MagicEntityDataInit.DIGGING_STATE, value)
    }

fun Player.markManaDataDirty() {
    val currentMaxMana = maxMana
    val currentManaAbsorptionRate = manaAbsorptionRate
    val currentMana = mana.coerceAtMost(currentMaxMana)

    maxMana = currentMaxMana
    manaAbsorptionRate = currentManaAbsorptionRate
    mana = currentMana

    val tracker = asHolder().getCooTracker()
    tracker.trackedTypes[MagicEntityDataInit.MAX_MANA.id] = MagicEntityDataInit.MAX_MANA
    tracker.trackedTypes[MagicEntityDataInit.CURRENT_MANA.id] = MagicEntityDataInit.CURRENT_MANA
    tracker.trackedTypes[MagicEntityDataInit.MANA_ABSORPTION_RATE.id] = MagicEntityDataInit.MANA_ABSORPTION_RATE
    tracker.trackedDirties[MagicEntityDataInit.MAX_MANA.id] = true
    tracker.trackedDirties[MagicEntityDataInit.CURRENT_MANA.id] = true
    tracker.trackedDirties[MagicEntityDataInit.MANA_ABSORPTION_RATE.id] = true
}

fun Player.syncManaDataToClient() {
    markManaDataDirty()
    TrackerManager.applyHolder(this)
    TrackerManager.schedulePlayerManaResync(this)
}

fun Player.isFullMana(): Boolean = mana >= maxMana
