package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
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
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import kotlin.math.PI
import kotlin.random.Random

class AntiEntityWandBarrage(
    damage: Double,
    shooter: PlayerEntity,
    loc: Vec3d,
    world: ServerWorld,
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
                velocity = Vec3d.ZERO
                speed = 0.0
                color = Math3DUtil.colorOf(240, 120, 255)
            })
        .apply {
            maxTick = options.maxLivingTick
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
            val entities = world.getEntitiesByClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            nearest = entities.minByOrNull {
                it.pos.distanceTo(loc)
            }
        }
        if (nearest != null) {
            direction = loc.relativize(nearest.pos)
//            direction = direction.add(loc.relativize(nearest.pos))
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun onHitDamaged(result: BarrageHitResult) {
        locusEmitters.cancelled = true
        for (entity in result.entities) {
            entity.timeUntilRegen = 0
            entity.hurtTime = 0
        }
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 6f, 1.2f
        )

        PointsBuilder()
            .addBall(1.0, 3)
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
}