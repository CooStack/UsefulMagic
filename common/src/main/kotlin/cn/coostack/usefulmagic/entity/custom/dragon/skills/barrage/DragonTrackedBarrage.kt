package cn.coostack.usefulmagic.entity.custom.dragon.skills.barrage

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.emitters.TailEmitter
import cn.coostack.usefulmagic.utils.EntityUtil
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*

class DragonTrackedBarrage(
    val trackingEntity: LivingEntity,
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    shooter: LivingEntity
) : EntityMagicDamagedBarrage(
    loc, world, BarrageOption()
        .acrossBlock()
        .maxLivingTick(20 * 10), damage, shooter
) {
    private var tick = 0
    var trackingTick = 20
    var trackingInternal = 20
    var trackingForce = 0.8
    var trackingMaxSpeed = 3.0


    var rightColor = Vector3f(1f, 0.5f, 1f)
    var leftColor = Vector3f(1f, 0.3f, 0f)
    override fun tick() {
        super.tick()
        tick++

        if (tick <= trackingInternal) {
            return
        }
        val maxTrackingTick = trackingInternal + trackingTick
        if (tick <= maxTrackingTick) {
            val currentDir = (trackingEntity.boxCenterPosition() - loc).normalize()
            val finalDir = currentDir * trackingForce + direction * options.speed
            options.speed = finalDir.length().coerceAtMost(trackingMaxSpeed)
            direction = finalDir.normalize()
        }
    }


    override fun onHitDamaged(result: BarrageHitResult) {
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return EntityUtil.filterDragon.test(livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(2.0, 2.0, 2.0)
    }

    override fun createControler(): ServerControler<*> {
        return TailEmitter(loc, world).apply {
            tailTemplate.apply {
                effect = ControlableEnchantmentEffect(uuid)
                color = this@DragonTrackedBarrage.leftColor
            }
            rightColor = this@DragonTrackedBarrage.rightColor
            simpleData.apply {
                minCount = 1
                maxCount = 2
                minAge = 7
                maxAge = 12
            }
            enableInterpolator = true
            emittersInterpolator.setRefiner(2.8)
                .setLimit(4.0)
            maxTick = -1
        }
    }

}