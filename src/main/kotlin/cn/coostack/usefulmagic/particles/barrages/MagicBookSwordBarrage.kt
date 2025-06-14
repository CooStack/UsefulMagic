package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import kotlin.math.PI
import kotlin.random.Random

class MagicBookSwordBarrage(
    loc: Vec3d,
    world: ServerWorld,
    hitBox: HitBox,
    damage: Double,
    speed: Double,
    shooter: MagicBookEntity
) : EntityDamagedBarrage(
    loc, world, hitBox, EndRodSwordStyle(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            acrossEmptyCollectionShape = true
            noneHitBoxTick = 114514
            maxLivingTick = -1
            enableSpeed = true
            this@apply.speed = speed
        }, damage, shooter
) {
    var maxTrackingTick = 10
    var tick = 0
    var target: LivingEntity? = null
    override fun tick() {
        super.tick()
        bindControl.rotateParticlesToPoint(RelativeLocation.of(direction))
        if (noclip()) return
        if (target == null) return
        if (tick++ < maxTrackingTick) {
            direction = direction.add(loc.relativize(target!!.pos).normalize()).normalize()
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun onHitDamaged(result: BarrageHitResult) {
        for (entity in result.entities) {
            entity.timeUntilRegen = 0
            entity.hurtTime = 0
        }
        // 不破坏方块 爆炸音效
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 6f, 1.2f
        )
        // 爆炸粒子
        PointsBuilder()
            .addBall(1.0, 8)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FIREWORK, world, loc, it.toVector().multiply(1 / 2.0), 64.0
                    )
            }
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid
    }
}