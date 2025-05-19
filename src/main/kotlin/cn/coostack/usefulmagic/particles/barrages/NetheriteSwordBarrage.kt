package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.util.Random
import kotlin.math.PI

class NetheriteSwordBarrage(loc: Vec3d, world: ServerWorld, val damage: Double, speed: Double) : AbstractBarrage(
    loc, world, HitBox.of(6.0, 6.0, 6.0),
    EndRodSwordStyle(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            acrossEmptyCollectionShape = true
            noneHitBoxTick = 5
            maxLivingTick = 180
            enableSpeed = true
            this@apply.speed = speed
        }
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return shooter?.uuid != livingEntity.uuid && livingEntity.isAlive
    }

    val traceBox = HitBox.of(
        48.0, 48.0, 48.0
    )
    var first = false
    var prevDirection = direction
    private var tickDelta = 0.0
    private var tick = 0
    override fun tick() {
        super.tick()
        tick++
        bindControl as EndRodSwordStyle
        val rotateTo = RelativeLocation()
        rotateTo.x = MathHelper.lerp(tickDelta, prevDirection.x, direction.x)
        rotateTo.y = MathHelper.lerp(tickDelta, prevDirection.y, direction.y)
        rotateTo.z = MathHelper.lerp(tickDelta, prevDirection.z, direction.z)
        tickDelta += 0.2
        bindControl.rotateParticlesToPoint(rotateTo)
        if (!first) {
            bindControl.rotateParticlesAsAxis(PI / Random(System.currentTimeMillis()).nextDouble(-1.0, 1.0))
            first = true
        }
        if (noclip()) {
            return
        }
        if (tick % 5 == 0) {
            val entities = world.getEntitiesByClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            val nearest = entities.minByOrNull {
                it.pos.distanceTo(loc)
            }
            prevDirection = direction
            if (nearest != null) {
                direction = loc.relativize(nearest.pos)
            }
            tickDelta = 0.0
        }
    }

    override fun onHit(result: BarrageHitResult) {
        val source = world.damageSources.playerAttack(shooter!! as PlayerEntity)
        for (entity in result.entities) {
            entity.damage(source, damage.toFloat())
            entity.resetPortalCooldown()
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
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FIREWORK, world, loc, it.toVector().multiply(1 / 2.0), 64.0
                    )
            }
    }
}