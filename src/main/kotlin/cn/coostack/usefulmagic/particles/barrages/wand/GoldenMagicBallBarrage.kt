package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.group.server.GoldenBallBarrageParticleServer
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.roundToInt

class GoldenMagicBallBarrage(
    loc: Vec3d,
    world: ServerWorld,
    val damage: Double,
) : AbstractBarrage(
    loc, world,
    HitBox.of(0.0, 0.0, 0.0),
    GoldenBallBarrageParticleServer(
        Vec3d(255.0, 255.0, 0.0),
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
        val source = world.damageSources.playerAttack(shooter as PlayerEntity)
        // 发射射线
        val box = Box.of(loc, 32.0, 32.0, 32.0)
        val targets = world.getEntitiesByClass(LivingEntity::class.java, box, ::filterHitEntity)
            .asSequence().take(6).toList()
        targets.forEach {
            val to = it.eyePos
            PointsBuilder().addLine(
                loc, to, loc.distanceTo(to).roundToInt() * 6
            ).create().forEach { it ->
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.ENCHANT, world, it.toVector(), Vec3d.ZERO, 64.0
                    )
            }
            it.damage(source, damage.toFloat())
            it.hurtTime = 0
            it.timeUntilRegen = 0
        }
        // 发出音效
        if (targets.isNotEmpty()) {
            world.playSound(
                null,
                loc.x,
                loc.y,
                loc.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
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
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
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
                        ParticleTypes.END_ROD, world, loc, it.toVector().multiply(1 / 2.0), 64.0
                    )
            }
    }
}