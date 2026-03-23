package cn.coostack.usefulmagic.particles.barrages.entity.skill

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.TailEmitter
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

class TrackedPointBarrage(
    val trackedTarget: LivingEntity,
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    shooter: LivingEntity
) :
    EntityMagicDamagedBarrage(
        loc, world,
        BarrageOption()
            .enableSpeedWithOptions(0.8)
            .maxLivingTick(20 * 10)
            .acrossBlock(true),
        damage,
        shooter
    ) {
    companion object {
        const val TRACKING_TICK = 22
        const val START_TRACKING_TICK = 10
    }

    var leftColor = Math3DUtil.colorOf(255, 212, 88)
    var rightColor = Math3DUtil.colorOf(117, 109, 156)

    var tick = 0
    private var startTracking = false

    override fun onHitDamaged(result: BarrageHitResult) {
        result.entities.forEach {
            it.invulnerableTime /= 2
        }
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        val currentShooter = shooter ?: return false
        if (livingEntity is MagicSubEyeEntity || livingEntity is MagicEyeEntity || livingEntity is MagicDragonEntity) {
            return false
        }
        return livingEntity.uuid != currentShooter.uuid && FriendFilterHelper.filterNotFriend(
            currentShooter,
            livingEntity
        )
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(2.0, 2.0, 2.0)
    }

    override fun createControler(): ServerControler<*> {
        return TailEmitter(loc, world)
            .apply {
                maxTick = -1
                enableInterpolator = true
                emittersInterpolator.setRefiner(4.0)
                leftColor = this@TrackedPointBarrage.leftColor
                rightColor = this@TrackedPointBarrage.rightColor
            }
    }


    override fun tick() {
        if (tick++ >= START_TRACKING_TICK && !startTracking) {
            startTracking = true
            tick = 0
            return
        }
        if (tick < TRACKING_TICK && startTracking) {
            // 玩家吸引力
            val dir = (trackedTarget.boxCenterPosition() - loc).normalize()
            direction = direction.normalize() * options.speed * 0.7 + dir * 1.3
            options.speed = direction.length().coerceAtMost(1.0)
        }


        super.tick()
    }
}
