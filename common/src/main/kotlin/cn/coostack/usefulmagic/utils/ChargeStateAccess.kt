package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.data.tracked.CooTrackerHolder
import cn.coostack.usefulmagic.data.tracked.TrackerManager
import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object ChargeStateAccess {
    fun isCharging(entity: Entity): Boolean {
        return holder(entity).getCooTracker().getOrDefault(MagicEntityDataInit.CHARGING, false)
    }

    fun setCharging(entity: Entity, value: Boolean) {
        holder(entity).getCooTracker().set(MagicEntityDataInit.CHARGING, value)
    }

    fun getChargingTick(entity: Entity): Int {
        return holder(entity).getCooTracker().getOrDefault(MagicEntityDataInit.CHARGE_TICK, 0)
    }

    fun setChargingTick(entity: Entity, value: Int) {
        holder(entity).getCooTracker().set(MagicEntityDataInit.CHARGE_TICK, value)
    }

    fun getChargedItem(entity: Entity): ItemStack {
        return holder(entity).getCooTracker().getOrDefault(MagicEntityDataInit.CHARGED_ITEM, ItemStack.EMPTY)
    }

    fun setChargedItem(entity: Entity, value: ItemStack) {
        holder(entity).getCooTracker().set(MagicEntityDataInit.CHARGED_ITEM, value)
    }

    fun getChargedItemIdentity(entity: Entity): Int {
        return holder(entity).getCooTracker().getOrDefault(MagicEntityDataInit.CHARGED_ITEM_IDENTITY, 0)
    }

    fun setChargedItemIdentity(entity: Entity, value: Int) {
        holder(entity).getCooTracker().set(MagicEntityDataInit.CHARGED_ITEM_IDENTITY, value)
    }

    fun reset(entity: Entity) {
        setCharging(entity, false)
        setChargingTick(entity, 0)
        setChargedItem(entity, ItemStack.EMPTY)
        setChargedItemIdentity(entity, 0)
        if (!entity.level().isClientSide) {
            TrackerManager.applyHolder(entity)
            if (entity is Player) {
                TrackerManager.schedulePlayerChargeStateResync(entity)
            }
        }
    }

    private fun holder(entity: Entity): CooTrackerHolder {
        return entity as CooTrackerHolder
    }
}
