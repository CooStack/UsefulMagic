package cn.coostack.usefulmagic.data.tracked

import cn.coostack.usefulmagic.extend.asHolder
import cn.coostack.usefulmagic.packet.s2c.PacketS2CTrackerToggle
import cn.coostack.usefulmagic.platform.UsefulMagicServices
import net.minecraft.world.entity.Entity

/**
 * 数据跟踪器，
 * 通过实体class注册，他和他的子类均有此方法，需要手动写ID用来区分
 */
object TrackerManager {
    private val holdersServer = HashSet<Entity>()
    private val pendingHoldersServer = HashSet<Entity>()
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

    fun tickOnServer() {
        tickingServer = true
        try {
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

}