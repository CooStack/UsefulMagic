package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ExplosionUtil {

    fun createRoundExplosion(
        world: World,
        center: Vec3d,
        power: Float,
        radius: Double,
        entity: LivingEntity,
        count: Int,
        sourceType: World.ExplosionSourceType
    ) {
        Math3DUtil.getPolygonInCircleVertices(count, radius)
            .forEach {
                it.add(RelativeLocation.of(center))
                world.createExplosion(entity, it.x, it.y, it.z, power, sourceType)
            }
    }
}