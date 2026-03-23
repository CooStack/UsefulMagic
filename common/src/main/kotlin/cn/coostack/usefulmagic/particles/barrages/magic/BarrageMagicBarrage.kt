package cn.coostack.usefulmagic.particles.barrages.magic

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.particles.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.magic.BarrageTailEmitter
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

class BarrageMagicBarrage(
    damage: Double,
    shooter: LivingEntity,
    loc: Vec3,
    world: ServerLevel,
) : EntityMagicDamagedBarrage(
    loc, world, BarrageOption()
        .acrossBlock(false)
        .acrossLiquid()
        .enableSpeedWithOptions(0.5)
        .enableAccelerationWithOptions(0.03)
        .accelerationMaxSpeed(3.0), damage, shooter
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(1.0, 1.0, 1.0)
    }

    override fun createControler(): ServerControler<*> {
        return BarrageTailEmitter(loc, world)
    }


    override fun onHitDamaged(result: BarrageHitResult) {
        result.entities.forEach {
//            it.remainingFireTicks = 120
            it.invulnerableTime /= 3
        }
    }
}

