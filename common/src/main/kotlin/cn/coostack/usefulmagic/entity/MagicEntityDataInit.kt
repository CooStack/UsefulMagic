package cn.coostack.usefulmagic.entity

import cn.coostack.usefulmagic.data.tracked.CooDataTracker
import cn.coostack.usefulmagic.data.tracked.CooTrackedData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

object MagicEntityDataInit {
    @JvmField
    val MAX_MANA = CooDataTracker.register(
        CooTrackedData(Int::class.java, "max_mana")
    )

    @JvmField
    val CURRENT_MANA = CooDataTracker.register(
        CooTrackedData(Int::class.java, "current_mana")
    )

    @JvmField
    val MANA_ABSORPTION_RATE = CooDataTracker.register(
        CooTrackedData(Int::class.java, "mana_absorption_rate")
    )

    @JvmField
    val CHARGING = CooDataTracker.register(
        CooTrackedData(Boolean::class.java, "charging")
    )

    @JvmField
    val CHARGE_TICK = CooDataTracker.register(
        CooTrackedData(Int::class.java, "charge_tick")
    )

    @JvmField
    val CHARGED_ITEM = CooDataTracker.register(
        CooTrackedData(ItemStack::class.java, "charged_item")
    )

    @JvmField
    val CHARGED_ITEM_IDENTITY = CooDataTracker.register(
        CooTrackedData(Int::class.java, "charged_item_identity")
    )

    @JvmField
    val VEC3_TARGET = CooDataTracker.register(
        CooTrackedData(Vec3::class.java, "vec3_target")
    )

    @JvmField
    val HAS_VEC3_TARGET = CooDataTracker.register(
        CooTrackedData(Boolean::class.java, "has_vec3_target")
    )

    fun init() {

    }

}
