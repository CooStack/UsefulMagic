package cn.coostack.usefulmagic.particles.barrages.api

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

abstract class PlayerDamagedBarrage(
    loc: Vec3,
    world: ServerLevel,
    hitBox: HitBox,
    bindControl: ServerControler<*>,
    options: BarrageOption,
    damage: Double,
    shooter: Player
) : EntityDamagedBarrage(loc, world, hitBox, bindControl, options, damage, shooter) {
    init {
        super.shooter = shooter
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            val source = it.damageSources().playerAttack(shooter!! as Player)
            it.hurt(source, damage.toFloat())
        }
        onHitDamaged(result)
    }

}