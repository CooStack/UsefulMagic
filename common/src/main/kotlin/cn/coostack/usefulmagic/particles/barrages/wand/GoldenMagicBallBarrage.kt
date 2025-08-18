package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.group.server.GoldenBallBarrageParticleServer
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

class GoldenMagicBallBarrage(
    loc: Vec3,
    world: ServerLevel,
    val damage: Double,
) : AbstractBarrage(
    loc, world,
    HitBox.of(0.0, 0.0, 0.0),
    GoldenBallBarrageParticleServer(
        Vec3(255.0, 255.0, 0.0),
        0.2f, 1.5, 16
    ), BarrageOption().apply {
        enableSpeed = true
        speed = 4.0 / 20
        acrossable = true
        maxAcrossCount = 16384
        maxLivingTick = 20 * 8
        acrossLiquid = true
        acrossBlock = true
    }
) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.isAlive && livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(
            shooter!!,
            livingEntity.uuid
        )
    }

    private var tick = 0
    override fun tick() {
        super.tick()
        if (tick++ % 8 != 0) {
            return
        }
        val source = world.damageSources().playerAttack(shooter as Player)
        // 发射射线
        val box = AABB.ofSize(loc, 32.0, 32.0, 32.0)
        val targets = world.getEntitiesOfClass(LivingEntity::class.java, box, ::filterHitEntity)
            .asSequence().take(6).toList()
        targets.forEach {
            val to = it.eyePosition
            PointsBuilder().addLine(
                loc, to, loc.distanceTo(to).roundToInt() * 6
            ).create().forEach { it ->
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.ENCHANT, world, it.toVector(), Vec3.ZERO, 64.0
                    )
            }
            it.hurt(source, damage.toFloat())
            it.hurtTime = 0
        }
        // 发出音效
        if (targets.isNotEmpty()) {
            world.playSound(
                null,
                loc.x,
                loc.y,
                loc.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                3f,
                2f
            )
        }

    }

    override fun onHit(result: BarrageHitResult) {
        world.playSound(
            null,
            loc.x,
            loc.y,
            loc.z,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            3f,
            1.5f
        )
        // 不处理
        PointsBuilder()
            .addBall(1.0, 6)
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.END_ROD, world, loc, it.toVector().scale(1 / 2.0), 64.0
                    )
            }
    }
}