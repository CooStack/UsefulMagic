package cn.coostack.usefulmagic.barrages.entity

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.particles.composition.SingleBarrageComposition
import cn.coostack.usefulmagic.particles.emitters.magic.BarrageTailEmitter
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

class EntityMagicWoodenBarrage(
    damage: Double,
    shooter: LivingEntity,
    loc: Vec3,
    world: ServerLevel,
    val burn: Boolean,
) : cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage(
    loc, world, BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            enableSpeed = true
            speed = 1.5
            noneHitBoxTick = 0
        }, damage, shooter
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(1.0, 1.0, 1.0)
    }

    override fun createControler(): ServerControler<*> {
        return SingleBarrageComposition(loc, world)
    }

    val emitter = BarrageTailEmitter(loc, world)
        .apply {
            template.color = Math3DUtil.colorOf(255, 100, 100)
            template.maxAge = 30
            template.effect = ControlableFireworkEffect(template.uuid)
            maxTick = -1
        }
    var first = false
    override fun tick() {
        super.tick()
        if (!first) {
            ParticleEmittersManager.spawnEmitters(emitter)
            first = true
        }
        emitter.teleportTo(loc)
        if (burn) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.FLAME, world, loc, Vec3.ZERO, true, 0.03, 1
            )
        }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        emitter.canceled = true
        if (!burn) {
            return
        }
        result.entities.forEach {
            it.remainingFireTicks = 120
        }
    }
}


