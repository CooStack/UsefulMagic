package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicHeartEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

object EntityUtil {

    val filterDragon = Predicate<LivingEntity> {
        it !is MagicDragonEntity && it !is MagicEyeEntity && it !is MagicSubEyeEntity && it !is MagicHeartEntity &&
                it.isAlive && !it.hasInfiniteMaterials()
    }

    val filterEye = filterDragon

    val filterSubEye = filterEye


    fun resetMovement(target: LivingEntity) {
        target.speed = 0f
        target.deltaMovement = Vec3.ZERO
        target.hurtMarked = true
    }

    fun findEntities(
        world: Level,
        center: Vec3,
        boxSize: Double,
        filter: Predicate<LivingEntity>
    ): List<LivingEntity> {
        return world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(center, boxSize, boxSize, boxSize),
            filter
        )
    }
}
