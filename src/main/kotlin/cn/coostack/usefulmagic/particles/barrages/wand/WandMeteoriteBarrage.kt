package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteTargetStyle
import cn.coostack.usefulmagic.utils.FallingBlockHelper
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.ParticleOption
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
        return livingEntity.uuid != shooter?.uuid  && FriendFilterHelper.filterNotFriend(shooter!!,livingEntity.uuid)
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
            val waveEmitters = ParticleWaveEmitters(
                spawnPos, world
            ).apply {
                maxTick = -1
                templateData.also {
                    it.effect = ControlableCloudEffect(it.uuid)
                    it.maxAge = 50
                    it.size = 0.6f
                }
                waveSpeed = 0.5
                delay = 5
            }
            val cloudEmitters = SimpleParticleEmitters(spawnPos, world, ControlableParticleData().apply {
                this.effect = ControlableCloudEffect(this.uuid)
                maxAge = 80
                speed = 0.1
                size = 0.4f
            }).apply {
                maxTick = -1
                this.count = 60
                shootType = EmittersShootTypes.box(
                    HitBox.of(8.0, 8.0, 8.0)
                )
            }
            val flameEmitters = SimpleParticleEmitters(spawnPos, world, ControlableParticleData().apply {
                this.effect = ControlableFireworkEffect(this.uuid)
                maxAge = 80
                color = Math3DUtil.colorOf(255, 200, 10)
                speed = 0.1
            }).apply {
                shootType = EmittersShootTypes.box(
                    HitBox.of(8.0, 8.0, 8.0)
                )
                maxTick = -1
                this.count = 80
            }
            ParticleEmittersManager.spawnEmitters(waveEmitters)
            ParticleEmittersManager.spawnEmitters(flameEmitters)
            ParticleEmittersManager.spawnEmitters(cloudEmitters)
            val meteorite = OptionMeteorite(
                Blocks.MAGMA_BLOCK, 5
            ) { loc ->
                waveEmitters.cancelled = true
                flameEmitters.cancelled = true
                cloudEmitters.cancelled = true
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

                ParticleEmittersManager.spawnEmitters(
                    ParticleWaveEmitters(
                        loc, world
                    ).apply {
                        maxTick = 1
                        waveCircleCountMax = 180
                        waveCircleCountMin = 160
                        templateData.also {
                            it.effect = ControlableCloudEffect(it.uuid)
                        }
                    }
                )

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

                val blocks = FallingBlockHelper.getBoxIncludeBlockPosList(
                    Box.of(spawnPos, 16.0, 1.0, 16.0), world
                )

                FallingBlockHelper.conversionBlockToFallingBlocks(
                    blocks, false, world
                ).forEach {
                    val direction = loc.relativize(it.pos).multiply(1.0 / 9)
                    it.velocity = direction.add(
                        random.nextDouble(-0.5, 0.5),
                        random.nextDouble(0.2, 0.5),
                        random.nextDouble(-0.5, 0.5)
                    )
                }

                val countDouble = ParticleOption.getParticleCounts()
                val explosion = ExplodeMagicEmitters(loc, world).apply {
                    this.templateData.also {
                        it.color = Math3DUtil.colorOf(255, 170, 200)
                        it.size = 0.4f
                    }
                    randomParticleAgeMin = 30
                    randomParticleAgeMax = 80
                    precentDrag = 0.95
                    maxTick = 5
                    ballCountPow = countDouble * 15
                    minSpeed = 0.5
                    maxSpeed = 7.0
                    randomCountMin = 300 * countDouble
                    randomCountMax = 400 * countDouble
                }
                ParticleEmittersManager.spawnEmitters(explosion)
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
                SimpleParticleEmitters(origin, world, ControlableParticleData().apply {
                    this.effect = ControlableCloudEffect(this.uuid)
                    maxAge = 80
                    speed = 0.2
                }).apply {
                    maxTick = 1
                    this.count = 40
                }
                flameEmitters.pos = origin
                cloudEmitters.pos = origin
                waveEmitters.pos = this.origin
                waveEmitters.waveAxis = direction.toVector()

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
                meteorite.direction.add(RelativeLocation.of(spawnPos.relativize(loc)))
            }
            meteorite.speed = 70.0 / 20
            meteorite.spawn(spawnPos, world)
        }
    }
}