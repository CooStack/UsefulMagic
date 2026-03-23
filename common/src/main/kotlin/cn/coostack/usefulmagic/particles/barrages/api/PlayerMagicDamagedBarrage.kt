package cn.coostack.usefulmagic.particles.barrages.api

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

abstract class PlayerMagicDamagedBarrage(
    loc: Vec3,
    world: ServerLevel,
    options: BarrageOption,
    damage: Double,
    shooter: Player
) : EntityMagicDamagedBarrage(loc, world, options, damage, shooter) {
    init {
        super.shooter = shooter
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            val source = UsefulMagicDamageSources.entityMagic(it.level(), shooter!!, shooter!!)
            it.hurt(source, damage.toFloat())
        }
        onHitDamaged(result)
    }

}