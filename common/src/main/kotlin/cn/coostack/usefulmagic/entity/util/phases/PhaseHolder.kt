package cn.coostack.usefulmagic.entity.util.phases

import net.minecraft.world.entity.LivingEntity
import java.util.function.Supplier

class PhaseHolder<T : LivingEntity>(val id: String, val supplier: Supplier<PhaseDefinition<T>>) {
    fun get() = supplier.get()
}
