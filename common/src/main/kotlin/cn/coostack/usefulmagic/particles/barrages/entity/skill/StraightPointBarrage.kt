package cn.coostack.usefulmagic.particles.barrages.entity.skill

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.particles.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.TailEmitter
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

class StraightPointBarrage(
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    shooter: LivingEntity
) :
    EntityMagicDamagedBarrage(
        loc, world, BarrageOption()
            .enableSpeedWithOptions(1.3)
            .maxLivingTick(20 * 10)
            .acrossBlock(true), damage, shooter
    ) {
    override fun onHitDamaged(result: BarrageHitResult) {

        result.entities.forEach {
            it.invulnerableTime = 0
        }
    }

    var leftColor = Math3DUtil.colorOf(255, 212, 88)
    var rightColor = Math3DUtil.colorOf(117, 109, 156)
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        if (livingEntity is MagicEyeEntity || livingEntity is MagicSubEyeEntity || livingEntity is MagicDragonEntity) {
            return false
        }
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(2.0, 2.0, 2.0)
    }

    override fun createControler(): ServerControler<*> {
        return TailEmitter(loc, world)
            .apply {
                maxTick = -1
                enableInterpolator = true
                emittersInterpolator.setRefiner(3.4)
                this.leftColor = this@StraightPointBarrage.leftColor
                this.rightColor = this@StraightPointBarrage.rightColor
            }
    }
}
