package cn.coostack.usefulmagic.data.tracked

import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import cn.coostack.usefulmagic.extend.asHolder
import cn.coostack.usefulmagic.packet.s2c.PacketS2CTrackerToggle
import cn.coostack.usefulmagic.platform.UsefulMagicServices
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

/**
 * 数据跟踪器，
 * 通过实体class注册，他和他的子类均有此方法，需要手动写ID用来区分
 */
object TrackerManager {
    private const val PLAYER_MANA_RESYNC_TICKS = 20
    private const val PLAYER_CHARGE_RESYNC_TICKS = 20

    private val holdersServer = HashSet<Entity>()
    private val pendingHoldersServer = HashSet<Entity>()
    private val delayedPlayerManaSyncs = HashMap<Player, Int>()
    private val delayedPlayerChargeStateSyncs = HashMap<Player, Int>()
    private var tickingServer = false

    fun applyHolder(holder: Entity) {
        if (holder.level().isClientSide) {
            return
        }
        if (tickingServer) {
            pendingHoldersServer.add(holder)
            return
        }
        holdersServer.add(holder)
    }

    fun schedulePlayerManaResync(player: Player, ticks: Int = PLAYER_MANA_RESYNC_TICKS) {
        if (player.level().isClientSide) {
            return
        }
        delayedPlayerManaSyncs[player] = maxOf(delayedPlayerManaSyncs[player] ?: 0, ticks)
        applyHolder(player)
    }

    fun schedulePlayerChargeStateResync(player: Player, ticks: Int = PLAYER_CHARGE_RESYNC_TICKS) {
        if (player.level().isClientSide) {
            return
        }
        delayedPlayerChargeStateSyncs[player] = maxOf(delayedPlayerChargeStateSyncs[player] ?: 0, ticks)
        applyHolder(player)
    }

    fun tickOnServer() {
        tickingServer = true
        try {
            tickDelayedPlayerManaSyncs()
            tickDelayedPlayerChargeStateSyncs()
            val iterator = holdersServer.iterator()
            while (iterator.hasNext()) {
                val holder = iterator.next()
                if (!holder.isAlive) {
                    iterator.remove()
                    continue
                }
                val tracker = holder.asHolder().getCooTracker()
                val hasDirtyArgs = tracker.trackedDirties.values.any { dirty -> dirty }
                if (hasDirtyArgs) {
                    UsefulMagicServices.PLATFORM.getIterateTracker().trackWith(
                        holder, PacketS2CTrackerToggle(
                            tracker, holder.id
                        ), true
                    )
                }
            }
        } finally {
            tickingServer = false
            if (pendingHoldersServer.isNotEmpty()) {
                holdersServer.addAll(pendingHoldersServer)
                pendingHoldersServer.clear()
            }
        }
    }

    private fun tickDelayedPlayerManaSyncs() {
        val iterator = delayedPlayerManaSyncs.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val player = entry.key
            val remainingTicks = entry.value
            if (!player.isAlive || player.level().isClientSide) {
                iterator.remove()
                continue
            }

            markPlayerManaDirty(player)
            holdersServer.add(player)

            if (remainingTicks <= 1) {
                iterator.remove()
            } else {
                entry.setValue(remainingTicks - 1)
            }
        }
    }

    private fun tickDelayedPlayerChargeStateSyncs() {
        val iterator = delayedPlayerChargeStateSyncs.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val player = entry.key
            val remainingTicks = entry.value
            if (!player.isAlive || player.level().isClientSide) {
                iterator.remove()
                continue
            }

            markPlayerChargeStateDirty(player)
            holdersServer.add(player)

            if (remainingTicks <= 1) {
                iterator.remove()
            } else {
                entry.setValue(remainingTicks - 1)
            }
        }
    }

    private fun markPlayerManaDirty(player: Player) {
        val tracker = player.asHolder().getCooTracker()
        val maxMana = tracker.getOrDefault(MagicEntityDataInit.MAX_MANA, 100)
        val manaAbsorptionRate = tracker.getOrDefault(MagicEntityDataInit.MANA_ABSORPTION_RATE, 1)
        val currentMana = tracker.getOrDefault(MagicEntityDataInit.CURRENT_MANA, 100).coerceAtMost(maxMana)

        tracker.trackedData[MagicEntityDataInit.MAX_MANA.id] = maxMana
        tracker.trackedData[MagicEntityDataInit.MANA_ABSORPTION_RATE.id] = manaAbsorptionRate
        tracker.trackedData[MagicEntityDataInit.CURRENT_MANA.id] = currentMana
        tracker.trackedTypes[MagicEntityDataInit.MAX_MANA.id] = MagicEntityDataInit.MAX_MANA
        tracker.trackedTypes[MagicEntityDataInit.MANA_ABSORPTION_RATE.id] = MagicEntityDataInit.MANA_ABSORPTION_RATE
        tracker.trackedTypes[MagicEntityDataInit.CURRENT_MANA.id] = MagicEntityDataInit.CURRENT_MANA
        tracker.trackedDirties[MagicEntityDataInit.MAX_MANA.id] = true
        tracker.trackedDirties[MagicEntityDataInit.MANA_ABSORPTION_RATE.id] = true
        tracker.trackedDirties[MagicEntityDataInit.CURRENT_MANA.id] = true
    }

    private fun markPlayerChargeStateDirty(player: Player) {
        val tracker = player.asHolder().getCooTracker()
        val charging = tracker.getOrDefault(MagicEntityDataInit.CHARGING, false)
        val chargingTick = tracker.getOrDefault(MagicEntityDataInit.CHARGE_TICK, 0)
        val chargedItem = tracker.getOrDefault(MagicEntityDataInit.CHARGED_ITEM, ItemStack.EMPTY)
        val chargedItemIdentity = tracker.getOrDefault(MagicEntityDataInit.CHARGED_ITEM_IDENTITY, 0)

        tracker.trackedData[MagicEntityDataInit.CHARGING.id] = charging
        tracker.trackedData[MagicEntityDataInit.CHARGE_TICK.id] = chargingTick
        tracker.trackedData[MagicEntityDataInit.CHARGED_ITEM.id] = chargedItem
        tracker.trackedData[MagicEntityDataInit.CHARGED_ITEM_IDENTITY.id] = chargedItemIdentity
        tracker.trackedTypes[MagicEntityDataInit.CHARGING.id] = MagicEntityDataInit.CHARGING
        tracker.trackedTypes[MagicEntityDataInit.CHARGE_TICK.id] = MagicEntityDataInit.CHARGE_TICK
        tracker.trackedTypes[MagicEntityDataInit.CHARGED_ITEM.id] = MagicEntityDataInit.CHARGED_ITEM
        tracker.trackedTypes[MagicEntityDataInit.CHARGED_ITEM_IDENTITY.id] = MagicEntityDataInit.CHARGED_ITEM_IDENTITY
        tracker.trackedDirties[MagicEntityDataInit.CHARGING.id] = true
        tracker.trackedDirties[MagicEntityDataInit.CHARGE_TICK.id] = true
        tracker.trackedDirties[MagicEntityDataInit.CHARGED_ITEM.id] = true
        tracker.trackedDirties[MagicEntityDataInit.CHARGED_ITEM_IDENTITY.id] = true
    }

}
