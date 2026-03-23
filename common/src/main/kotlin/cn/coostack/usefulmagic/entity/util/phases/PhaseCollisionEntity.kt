package cn.coostack.usefulmagic.entity.util.phases

import net.minecraft.world.entity.LivingEntity

interface PhaseCollisionEntity<T : LivingEntity> {
    fun collisionEntity(targets: Set<LivingEntity>, instance: T, runtime: PhaseRuntime)
}
