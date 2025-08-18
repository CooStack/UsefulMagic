package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.api.PlayerDamagedBarrage
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import java.util.Random
import kotlin.math.PI

class NetheriteSwordBarrage(loc: Vec3, world: ServerLevel, damage: Double, shooter: Player, speed: Double) :
    PlayerDamagedBarrage(
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
            }, damage, shooter
    ) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return shooter?.uuid != livingEntity.uuid && livingEntity.isAlive && FriendFilterHelper.filterNotFriend(
            shooter!!,
            livingEntity.uuid
        )
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
        rotateTo.x = Mth.lerp(tickDelta, prevDirection.x, direction.x)
        rotateTo.y = Mth.lerp(tickDelta, prevDirection.y, direction.y)
        rotateTo.z = Mth.lerp(tickDelta, prevDirection.z, direction.z)
        tickDelta += 0.2
        bindControl.rotateParticlesToPoint(rotateTo)
        if (noclip()) {
            return
        }
        if (tick % 5 == 0) {
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            val nearest = entities.minByOrNull {
                it.position().distanceTo(loc)
            }
            prevDirection = direction
            if (nearest != null) {
                direction = loc.relativize(nearest.position())
            }
            tickDelta = 0.0
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun onHitDamaged(result: BarrageHitResult) {
        for (entity in result.entities) {
            entity.hurtTime = 0
        }
        // 不破坏方块 爆炸音效
        world.playSound(
            null, loc.x, loc.y, loc.z, UsefulMagicSoundEvents.MAGIC_SWORD.get(), SoundSource.PLAYERS, 6f, 1.2f
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
                        ParticleTypes.FIREWORK, world, loc, it.toVector().scale(1 / 2.0), 64.0
                    )
            }
    }
}