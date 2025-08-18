package cn.coostack.usefulmagic.particles.barrages.api

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.UUID

abstract class DamagedBarrage(
    loc: Vec3,
    world: ServerLevel,
    hitBox: HitBox,
    bindControl: ServerControler<*>,
    options: BarrageOption,
    var damage: Double,
) : AbstractBarrage(loc, world, hitBox, bindControl, options) {
    var offlineShooter: UUID? = null

    abstract fun onHitDamaged(result: BarrageHitResult)

}