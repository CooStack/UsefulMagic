package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.particles.style.EndRodExplosionStyle
import cn.coostack.usefulmagic.particles.style.WandMeteoriteStyle
import cn.coostack.usefulmagic.particles.style.WandMeteoriteTargetStyle
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.BlockStateParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.PI
import kotlin.random.Random

class WandMeteoriteBarrage(
    val damage: Double,
    override var shooter: LivingEntity?,
    loc: Vec3d,
    world: ServerWorld,
) : AbstractBarrage(
    loc, world, HitBox.of(2.0, 2.0, 2.0), SingleBarrageParticleServer(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            enableSpeed = true
            speed = 2.0
            noneHitBoxTick = 0
        }) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid
    }

    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.ENCHANT, world, loc, Vec3d.ZERO, true, 0.05, 1
        )
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.SLOWNESS, 240, 10), shooter
            )
        }

        val entity = result.entities.firstOrNull()
        entity?.let {
            val target = WandMeteoriteTargetStyle(it.id)
            ParticleStyleManager.spawnStyle(it.world as ServerWorld, it.eyePos, target)
        }
        val random = Random(System.currentTimeMillis())
        val d = Vec3d(random.nextDouble(-16.0, 16.0), 40.0, random.nextDouble(-16.0, 16.0))
        var spawnPos = loc.add(d)
        // 生成魔法阵
        val style = WandMeteoriteStyle(
            RelativeLocation.of(
                d
            )
        )
        ParticleStyleManager.spawnStyle(world, spawnPos, style)
        // 生成大陨石
        spawnPos = spawnPos.add(d)

        val create = PointsBuilder().addBall(64.0, 25).create()
        CooParticleAPI.scheduler.runTaskTimerMaxTick(100) {
            Math3DUtil.rotateAsAxis(create, RelativeLocation.yAxis(), random.nextDouble(-PI, PI))
            repeat(16) {
                val rl = create.random()
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FIREWORK, this@WandMeteoriteBarrage.world,
                    spawnPos.add(rl.toVector()), rl.normalize().multiply(-5.8).toVector(), 256.0
                )
            }
        }
        CooParticleAPI.scheduler.runTask(100) {
            val meteorite = OptionMeteorite(
                Blocks.MAGMA_BLOCK, 5
            ) { loc ->
                val box = Box.of(
                    loc, 20.0, 20.0, 20.0
                )
                val source =
                    if (shooter == null) world.damageSources.flyIntoWall() else world.damageSources.playerAttack(shooter as PlayerEntity)
                world.getEntitiesByClass(
                    LivingEntity::class.java, box
                ) {
                    it != shooter
                }.forEach {
                    it.damage(source, damage.toFloat())
                    it.hurtTime = 0
                    it.timeUntilRegen = 0
                }
                PointsBuilder()
                    .addCircle(3.0, 160).create().forEach {
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.CLOUD, this@WandMeteoriteBarrage.world, loc, it.toVector(), 256.0
                        )
                    }
                // 地面方块粒子上扬
                PointsBuilder().addRoundShape(8.0, 1.0, 10, 180)
                    .create().forEach {
                        val state = world.getBlockState(
                            BlockPos.ofFloored(
                                loc
                            )
                        )
                        val len = 16 / it.length()
                        repeat(len.toInt()) { i ->
                            ServerParticleUtil.spawnSingle(
                                BlockStateParticleEffect(ParticleTypes.BLOCK, state), world!!,
                                loc.add(it.toVector().add(0.0, i + 1.0, 0.0)),
                                Vec3d.ZERO
                            )
                        }
                    }
                // 爆炸散开的末地烛
                val explosion = EndRodExplosionStyle()
                    .apply {
                        r = 255
                        g = 170
                        b = 200
                        minBallCountPow = 4
                        maxBallCountPow = 16
                        ballStep = 5.0
                        ballRadius = 150.0
                        maxAge = 110
                        particleSize = 0.4f
                        explosionScaleTick = 120
                        c1 = RelativeLocation(
                            5.0, 0.9, 0.0
                        )
                        c1 = RelativeLocation(
                            -110.0, 0.0, 0.0
                        )
                    }
                ParticleStyleManager.spawnStyle(world, loc, explosion)
                val nd = d.normalize().multiply(-1.0)
                var cl = loc
                for (i in 1..10) {
                    world.createExplosion(
                        shooter,
                        source,
                        null,
                        cl,
                        10f,
                        false,
                        World.ExplosionSourceType.TNT
                    )
                    cl = cl.add(nd.multiply(2.0))
                }
            }.withTick {
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.SMOKE, this@WandMeteoriteBarrage.world, origin, Vec3d.ZERO, true, 0.2, 30, 256.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, this@WandMeteoriteBarrage.world, origin, Vec3d.ZERO, true, 0.2, 30, 256.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FLAME, this@WandMeteoriteBarrage.world, origin, Vec3d.ZERO, true, 0.2, 30, 256.0
                )

                PointsBuilder()
                    .addCircle(5.0, 60)
                    .rotateTo(d)
                    .pointsOnEach {
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.SMOKE, this@WandMeteoriteBarrage.world, origin.add(
                                it.toVector()
                            ), Vec3d.ZERO, true, 0.2, 1, 256.0
                        )
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.CLOUD, this@WandMeteoriteBarrage.world, origin.add(
                                it.toVector()
                            ), Vec3d.ZERO, true, 0.2, 1, 256.0
                        )
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.FLAME, this@WandMeteoriteBarrage.world, origin.add(
                                it.toVector()
                            ), Vec3d.ZERO, true, 0.2, 1, 256.0
                        )
                    }
                if (time % 5 == 0) {
                    PointsBuilder()
                        .addCircle(2.0, 80)
                        .pointsOnEach { it.y += 1 }
                        .rotateTo(d)
                        .create()
                        .forEach {
                            val l = origin.add(d.normalize().multiply(-6.0))
                            ServerParticleUtil.spawnSingle(
                                ParticleTypes.FIREWORK, this@WandMeteoriteBarrage.world, l, it.toVector(), 256.0
                            )
                        }
                }

                if (time % 5 == 0) {
                    world?.playSound(
                        null, origin.x, origin.y, origin.z,
                        SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 10F, 1F
                    )
                }
                if (entity != null) {
                    direction = RelativeLocation.of(
                        origin.relativize(entity.eyePos)
                    )
                }
            }
            if (entity == null) {
                meteorite.direction = RelativeLocation.of(spawnPos.relativize(loc))
            }
            meteorite.speed = 70.0 / 20
            meteorite.spawn(spawnPos, world)
        }
    }
}