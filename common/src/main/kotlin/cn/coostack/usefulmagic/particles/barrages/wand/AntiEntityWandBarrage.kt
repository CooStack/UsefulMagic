package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.api.PlayerDamagedBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandBarrageStyle
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random

class AntiEntityWandBarrage(
    damage: Double,
    shooter: Player,
    loc: Vec3,
    world: ServerLevel,
    hitBox: HitBox,
) : PlayerDamagedBarrage(
    loc, world, hitBox, AntiEntityWandBarrageStyle(),
    BarrageOption().apply {
        enableSpeed = true
        speed = 1.0
        maxLivingTick = 120
        noneHitBoxTick = 10
    }, damage, shooter
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return shooter?.uuid != livingEntity.uuid && livingEntity.isAlive && FriendFilterHelper.filterNotFriend(
            shooter!!,
            livingEntity.uuid
        )
    }

    val traceBox = HitBox.of(32.0, 32.0, 32.0)
    private var tick = 0
    private var lastLoc = loc
    val locusEmitters = SimpleParticleEmitters(
        loc, world, ControlableParticleData()
            .apply {
                maxAge = 10
                velocity = Vec3.ZERO
                speed = 0.01
                size = 0.4f
                color = Math3DUtil.colorOf(240, 120, 255)
            })
        .apply {
            maxTick = options.maxLivingTick
            delay = 0
            count = 6
        }

    override fun tick() {
        super.tick()
        if (!locusEmitters.playing) {
            ParticleEmittersManager.spawnEmitters(locusEmitters)
        }
        tick++
        locusEmitters.pos = loc
        lastLoc = loc
        if (noclip()) {
            return
        }
        var nearest: LivingEntity? = null
        if (tick % 5 == 0) {
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            nearest = entities.minByOrNull {
                it.position().distanceTo(loc)
            }
        }
        if (nearest != null) {
            direction = loc.relativize(nearest.position())
//            direction = direction.add(loc.relativize(nearest.pos))
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun onHitDamaged(result: BarrageHitResult) {
        locusEmitters.cancelled = true
        for (entity in result.entities) {
            entity.hurtTime = 0
        }
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 6f, 1.2f
        )

        PointsBuilder()
            .addBall(1.0, 2)
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