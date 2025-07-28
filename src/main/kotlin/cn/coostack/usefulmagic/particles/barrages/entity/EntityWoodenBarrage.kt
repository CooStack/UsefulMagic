package cn.coostack.usefulmagic.particles.barrages.entity

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.particles.barrages.api.EntityDamagedBarrage
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class EntityWoodenBarrage(
    damage: Double,
    shooter: LivingEntity,
    loc: Vec3d,
    world: ServerWorld,
    val burn: Boolean,
) : EntityDamagedBarrage(
    loc, world, HitBox.of(1.0, 1.0, 1.0), SingleBarrageParticleServer(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            enableSpeed = true
            speed = 1.5
            noneHitBoxTick = 0
        }, damage, shooter
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity.uuid)
    }

    val emitter = SimpleParticleEmitters(
        loc, world, ControlableParticleData()
            .apply {
                effect = ControlableFireworkEffect(uuid)
                maxAge = 30
                speed = 0.03
                color = Math3DUtil.colorOf(255, 100, 100)
            }
    ).apply {
        maxTick = -1
        count = 1
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
                ParticleTypes.FLAME, world, loc, Vec3d.ZERO, true, 0.03, 1
            )
        }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        emitter.cancelled = true
        if (!burn) {
            return
        }
        result.entities.forEach {
            it.setOnFireForTicks(120)
        }
    }
}