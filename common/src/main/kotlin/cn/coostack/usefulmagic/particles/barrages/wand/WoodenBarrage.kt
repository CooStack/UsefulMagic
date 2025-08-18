package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.usefulmagic.particles.barrages.api.PlayerDamagedBarrage
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

class WoodenBarrage(
    damage: Double,
    shooter: Player,
    loc: Vec3,
    world: ServerLevel,
    val burn: Boolean,
) : PlayerDamagedBarrage(
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

    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.ENCHANT, world, loc, Vec3.ZERO, true, 0.05, 1
        )
        if (burn) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.FLAME, world, loc, Vec3.ZERO, true, 0.03, 1
            )
        }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        if (!burn) {
            return
        }
        result.entities.forEach {
            it.remainingFireTicks = 120
        }
    }
}