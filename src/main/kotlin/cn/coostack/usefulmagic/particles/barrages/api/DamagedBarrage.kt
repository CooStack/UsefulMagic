package cn.coostack.usefulmagic.particles.barrages.api

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import java.util.UUID

abstract class DamagedBarrage(
    loc: Vec3d,
    world: ServerWorld,
    hitBox: HitBox,
    bindControl: ServerControler<*>,
    options: BarrageOption,
    var damage: Double,
) : AbstractBarrage(loc, world, hitBox, bindControl, options) {
    var offlineShooter: UUID? = null

    abstract fun onHitDamaged(result: BarrageHitResult)

}