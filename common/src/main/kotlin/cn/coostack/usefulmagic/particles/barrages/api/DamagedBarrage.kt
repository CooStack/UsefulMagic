package cn.coostack.usefulmagic.particles.barrages.api

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.UUID

abstract class DamagedBarrage(
    loc: Vec3,
    world: ServerLevel,
    options: BarrageOption,
    var damage: Double,
) : AbstractBarrage(loc, world, options) {
    var offlineShooter: UUID? = null

    abstract fun onHitDamaged(result: BarrageHitResult)

}