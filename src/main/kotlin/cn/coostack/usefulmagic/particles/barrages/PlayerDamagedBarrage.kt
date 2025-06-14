package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

abstract class PlayerDamagedBarrage(
    loc: Vec3d,
    world: ServerWorld,
    hitBox: HitBox,
    bindControl: ServerControler<*>,
    options: BarrageOption,
    damage: Double,
    shooter: PlayerEntity
) : EntityDamagedBarrage(loc, world, hitBox, bindControl, options, damage, shooter) {
    init {
        super.shooter = shooter
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            val source = it.damageSources.playerAttack(shooter!! as PlayerEntity)
            it.damage(source, damage.toFloat())
        }
        onHitDamaged(result)
    }

}