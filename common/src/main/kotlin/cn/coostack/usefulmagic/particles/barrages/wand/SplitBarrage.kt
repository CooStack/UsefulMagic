package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.ServerParticleGroup
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.api.PlayerDamagedBarrage
import cn.coostack.usefulmagic.particles.group.server.EnchantBallParticleServer
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

class SplitBarrage(
    loc: Vec3,
    world: ServerLevel,
    hitBox: HitBox,
    bindControl: ServerParticleGroup,
    options: BarrageOption,
    val subColor: Vec3,
    shooter: Player,
    damage: Double,
    /**
     * 不可分裂设 -1
     */
    val splitTime: Int,
    /**
     * 分割后
     */
    val splitCount: Int = 3,
    /**
     * 是否追踪实体
     */
    val trace: Boolean = false,
    /**
     * 追踪实体范围
     */
    val traceBox: HitBox = HitBox.of(20.0, 10.0, 20.0)
) : PlayerDamagedBarrage(loc, world, hitBox, bindControl, options, damage, shooter) {
    var current = 0
    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.FLAME,
            world,
            loc,
            Vec3.ZERO,
            true,
            0.1, 2, 64.0
        )

        // 判断跟踪
        if (trace && !noclip()) {
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            val nearest = entities.minByOrNull {
                it.position().distanceTo(loc)
            }
            if (nearest != null) {
                direction = loc.relativize(nearest.position())
            }
        }

        if (splitTime == -1) {
            return
        }
        if (current++ < splitTime) {
            return
        }
        // 不破坏方块 爆炸音效
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 6f, 1f
        )
        // 分裂粒子
        PointsBuilder()
            .addBall(1.0, 5)
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.CLOUD, world, loc, it.toVector().scale(1 / 5.0), 64.0
                    )
            }
        split()
        remove()
    }

    private fun split() {
        // 分裂操作
        val step = (3 * PI / 6) / splitCount
        var current = -step * (splitCount / 2)
        repeat(splitCount) {
            val sub = SplitBarrage(
                loc, world, HitBox.of(1.6, 1.6, 1.6),
                EnchantBallParticleServer(
                    subColor, 0.1f, 0.4, 8
                ), BarrageOption().apply {
                    speed = this@SplitBarrage.options.speed
                    enableSpeed = this@SplitBarrage.options.enableSpeed
                    maxLivingTick = 60
                    noneHitBoxTick = 10
                }, subColor, shooter as Player, damage / splitCount, -1, 0, true
            )
            sub.shooter = shooter
            sub.direction = Math3DUtil.rotateAsAxis(
                listOf(
                    RelativeLocation.of(direction)
                ), RelativeLocation.yAxis(), current
            )[0].toVector()
            BarrageManager.spawn(sub)
            current += step
        }
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid
                && livingEntity.isAlive
                && !livingEntity.noPhysics
                && FriendFilterHelper.filterNotFriend(shooter!!,livingEntity.uuid)
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        if (splitTime != -1) {
            split()
        }

        // 重置实体的damage source
        for (entity in result.entities) {
            entity.hurtTime = 0
        }

        // 不破坏方块 爆炸音效
        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 6f, 1.2f
        )
        // 爆炸粒子
        PointsBuilder()
            .addBall(1.0, 6)
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FLAME, world, loc, it.toVector().scale(1 / 2.0), 64.0
                    )
            }
    }
}