package cn.coostack.usefulmagic.barrages.entity

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.particles.composition.skill.SwordComposition
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.random.Random

class MagicBookSwordBarrageMagic(
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    speed: Double,
    shooter: MagicBookEntity
) : cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage(
    loc, world, BarrageOption()
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
        bindControl.get().runAsIfType<SwordComposition> {
            this.direction = this@MagicBookSwordBarrageMagic.direction.asRelative()
            markDirty()
        }
        if (noclip()) return
        if (target == null) return
        if (tick++ < maxTrackingTick) {
            direction = direction.add((target!!.position() - loc).normalize()).normalize()
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun onHitDamaged(result: BarrageHitResult) {
        for (entity in result.entities) {
            entity.invulnerableTime = 0
        }
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 6f, 1.2f
        )
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

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(4.0, 4.0, 4.0)
    }

    override fun createControler(): ServerControler<*> {
        return SwordComposition(loc, world)
    }
}

