package cn.coostack.usefulmagic.barrages.api

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

abstract class EntityMagicDamagedBarrage(
    loc: Vec3,
    world: ServerLevel,
    options: BarrageOption,
    damage: Double,
    shooter: LivingEntity,
) : cn.coostack.usefulmagic.barrages.api.DamagedBarrage(loc, world, options, damage) {
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