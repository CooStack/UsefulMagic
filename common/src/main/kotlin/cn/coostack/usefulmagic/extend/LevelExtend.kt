package cn.coostack.usefulmagic.extend

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

inline fun <reified T : LivingEntity> Level.searchEntities(
    center: Vec3,
    boxSize: Double,
    filter: Predicate<T>
): List<T> {
    return getEntitiesOfClass(T::class.java, AABB.ofSize(center, boxSize, boxSize, boxSize), filter)
}