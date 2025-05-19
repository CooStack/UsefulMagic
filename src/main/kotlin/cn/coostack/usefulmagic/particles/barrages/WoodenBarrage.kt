package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class WoodenBarrage(
    val damage: Double,
    override var shooter: LivingEntity?,
    loc: Vec3d,
    world: ServerWorld,
    val burn: Boolean
) : AbstractBarrage(
    loc, world, HitBox.of(1.0, 1.0, 1.0), SingleBarrageParticleServer(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            enableSpeed = true
            speed = 1.5
            noneHitBoxTick = 0
        }) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid
    }

    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.ENCHANT, world, loc, Vec3d.ZERO, true, 0.05, 1
        )
        if (burn) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.FLAME, world, loc, Vec3d.ZERO, true, 0.03, 1
            )
        }
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            if (burn) {
                it.setOnFireForTicks(120)
            }
            val source = it.damageSources.playerAttack(shooter!! as PlayerEntity)
            it.damage(source, damage.toFloat())

        }
    }
}