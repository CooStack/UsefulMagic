package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.cooparticlesapi.animation.AnimateManager
import cn.coostack.cooparticlesapi.animation.AnimateNode
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.animate.TickableAction
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.extend.hasVec3Target
import cn.coostack.usefulmagic.extend.vec3Target
import cn.coostack.usefulmagic.meteorite.MeteoriteImpactHelper
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionMagicBallComposition
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionMagicComposition
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionStarComposition
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.emitters.magic.ExplosionMagicCloudEmitter
import cn.coostack.usefulmagic.renderer.ExplosionBeamRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.AnimateStatus
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 因为设计缘故 只有玩家能释放这个魔法
 *
 * @constructor
 *
 * @param properties
 */
class ExplosionMagic(properties: Properties) : ChargingMagic<ExplosionMagicBallComposition>(properties) {
    companion object {
        private const val METEORITE_BASE_EXPLOSION_POWER = 2.0
        private const val METEORITE_BASE_RADIUS_MULTIPLIER = 1.25
        private const val EXPLOSION_RADIUS_SCALE = 3.5
        private const val ENTITY_DAMAGE_RADIUS_MULTIPLIER = 1.8
        private const val FULL_DAMAGE_RADIUS_MULTIPLIER = 0.75
        private const val IMPACT_BLOCK_TICK_BUDGET = 10
        private val IMPACT_DIRECTION = Vec3(0.0, -1.0, 0.0)
    }

    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 这里是爆炸释放
        // explosion !
        if (shooter !is Player) {
            return
        }
        val world = world as ServerLevel

        val target = shooter.vec3Target
        val damage = MagicHelper.getMagicDamage(wandStack)
        // 召唤beam
        // 召唤一个蘑菇云粒子
        handleExplodeParticle(world, shooter, target)
        // 召唤一个大范围爆炸 (比陨石大)
        handleExplode(world, shooter as ServerPlayer, target, damage)

        shooter.hasVec3Target = false
        shooter.vec3Target = Vec3.ZERO

    }


    override fun usingTick(shooter: LivingEntity, wandStack: ItemStack, ballStack: ItemStack, world: Level, time: Int) {
        if (shooter !is Player) {
            return
        }
        super.usingTick(shooter, wandStack, ballStack, world, time)
        // 这里是蓄力 召唤 Magic
        if (!shooter.hasVec3Target) {
            shooter.vec3Target = MagicHelper.findClipTarget(world, shooter, 200.0)
            shooter.hasVec3Target = true
        }


        // 这里要做动画整体偏移 （要获取总时长）
        val totalTime = MagicHelper.getMaxChargingTick(wandStack).coerceAtLeast(300)
        // 根据time播放音效
        if (time % 30 == 0) {
            world.playSound(
                null,
                shooter.x,
                shooter.y,
                shooter.z,
                SoundEvents.BEACON_AMBIENT,
                SoundSource.PLAYERS,
                3f,
                (time * 2 / totalTime).toFloat()
            )
        }
        // 要对他进行偏移
        val container = getOrCreateContainer(shooter)
        val animate = container.getOrCreate {
            AnimateStatus(
                // 初始化
                Animate()
                    .addNode(
                        AnimateNode()
                            // emitter
                            .addAction(TickableAction {
                                !shooter.charging
                            }.addPreTickAction {
                                if (tickCount % 5 != 0) {
                                    return@addPreTickAction
                                }
                                val p = Vec3.ZERO.random() * 45
                                val spawnPos = shooter.boxCenterPosition().add(p)
                                val emitter = ExplosionLineEmitters(spawnPos, shooter.level() as ServerLevel)
                                    .apply {
                                        this.maxTick = 80
                                        val targetPoint =
                                            shooter.eyePosition.add(shooter.forward.normalize().scale(5.0))
                                        this.targetPoint = targetPoint
                                        this.templateData.maxAge = 120
                                        this.templateData.speed = 1.2
                                        this.templateData.velocity = Vec3(
                                            random.nextDouble(-5.0, 5.0),
                                            random.nextDouble(-5.0, 5.0),
                                            random.nextDouble(-5.0, 5.0),
                                        )
                                    }
                                CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, totalTime - 40) {
                                    emitter.targetPoint =
                                        shooter.eyePosition.add(shooter.forward.normalize().scale(3.0))
                                }.setCancelPredicate {
                                    !shooter.charging
                                }
                                ParticleEmittersManager.spawnEmitters(emitter)
                            })
                    ).addNode(
                        AnimateNode()
                            // lightning
                            .addAction(TickableAction {
                                !shooter.charging
                            }.addPreTickAction {
                                if (tickCount % 3 != 0) {
                                    return@addPreTickAction
                                }
                                val startRadius = 5.0

                                repeat(Random.nextInt(2, 6)) {
                                    val sin = sin(Random.nextDouble(-PI, PI)) * startRadius
                                    val cos = cos(Random.nextDouble(-PI, PI)) * startRadius

                                    val randomPos = Vec3(
                                        cos,
                                        Random.nextDouble(-startRadius, startRadius),
                                        sin,
                                    )
                                    val spawnPos = shooter.boxCenterPosition().add(randomPos)
                                    val emitter = LightningParticleEmitters(
                                        spawnPos, world
                                    ).apply {
                                        targetPos = (randomPos.normalize() * Random.nextDouble(
                                            15.0,
                                            45.0
                                        ))
                                        simpleData.apply {
                                            minAge = 2
                                            maxAge = 5
                                            minCount = 1
                                            maxCount = 3
                                            minSize = 0.1
                                            maxSize = 0.3
                                        }
                                        maxTick = 1
                                        templateData.also {
                                            it.color = Math3DUtil.colorOf(
                                                121, 211, 249
                                            )
                                        }
                                    }
                                    ParticleEmittersManager.spawnEmitters(emitter)
                                }
                            })
                    )
                    .addNode(
                        // star
                        AnimateNode()
                            .addAction(TickableAction {
                                !shooter.charging
                            }.addPreTickAction {
                                val r = Random.nextDouble(3.0, 10.0)
                                val p = Vec3.ZERO.random() * r
                                val composition = ExplosionStarComposition(
                                    shooter.eyePosition.add(p),
                                    world
                                )
                                ParticleCompositionManager.spawn(composition)
                            }), totalTime - 100
                    )
                    .addNode(
                        AnimateNode()
                            // magic
                            .addAction(TickableAction {
                                !shooter.charging
                            }.apply {
                                val composition = ExplosionMagicComposition(
                                    shooter.vec3Target.add(0.0, 18.0, 0.0),
                                    world as ServerLevel
                                ).apply {
                                    rotateDirection = RelativeLocation.yAxis()
                                }
                                addStartActions {
                                    ParticleCompositionManager.spawn(composition)
                                }.addDoneAction {
                                    composition.remove()
                                }
                            }), totalTime - 240
                    ).addNode(
                        AnimateNode()
                            .addAction(TickableAction {
                                !shooter.charging
                            }.addStartActions {
                                ServerRenderEntityManager.spawn(ExplosionBeamRenderEntity(world, shooter.vec3Target))
                            }), totalTime - 20
                    )
            )
        }
        if (!animate.display) {
            AnimateManager.displayAnimateServer(animate)
        }
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        super.stopUse(shooter, world, wandStack, ballStack, chargingTick, max)
        // 停止 要去取消
        val container = getOrCreateContainer(shooter)
        val animate = container.get<Animate>() ?: return
        animate.cancel()
        shooter.vec3Target = Vec3.ZERO
        shooter.hasVec3Target = false
    }

    override fun onCompositionTick(
        composition: ExplosionMagicBallComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {

    }

    override fun getComposition(shooter: LivingEntity): ExplosionMagicBallComposition? =
        getOrCreateContainer(shooter)
            .get()

    override fun getOrCreateComposition(
        shooter: LivingEntity,
        world: Level
    ): ExplosionMagicBallComposition = getOrCreateContainer(shooter)
        .getOrCreate {
            controlEntryOf(shooter) {
                val composition = ExplosionMagicBallComposition(shooter.position(), world)
                    .apply {
                        player = shooter.uuid
                    }
                ParticleCompositionManager.spawn(composition)
                composition
            }
        } as ExplosionMagicBallComposition

    private fun handleExplode(
        world: ServerLevel,
        user: ServerPlayer,
        target: Vec3,
        damage: Double
    ) {
        val damageSource = world.damageSources().playerAttack(user)
        val explosionRadius =
            exp(METEORITE_BASE_EXPLOSION_POWER) * METEORITE_BASE_RADIUS_MULTIPLIER * EXPLOSION_RADIUS_SCALE
        val blockExplosionRadius = explosionRadius.roundToInt().coerceAtLeast(1)
        val entityDamageRadius = explosionRadius * ENTITY_DAMAGE_RADIUS_MULTIPLIER
        val fullDamageRadius = explosionRadius * FULL_DAMAGE_RADIUS_MULTIPLIER

        processMeteoriteStyleExplosion(world, user, target, blockExplosionRadius)

        world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(target, entityDamageRadius * 2.0, entityDamageRadius * 2.0, entityDamageRadius * 2.0)
        ) {
            it.uuid != user.uuid && it.isAlive
        }.forEach { entity ->
            val distance = entity.boundingBox.center.distanceTo(target)
            if (distance > entityDamageRadius) {
                return@forEach
            }
            val damageFactor = if (
                distance <= fullDamageRadius || fullDamageRadius >= entityDamageRadius
            ) {
                1.0
            } else {
                1.0 - ((distance - fullDamageRadius) / (entityDamageRadius - fullDamageRadius)).coerceIn(0.0, 1.0)
            }
            val finalDamage = (damage * damageFactor).toFloat()
            if (finalDamage <= 0.05f) {
                return@forEach
            }
            entity.hurt(damageSource, finalDamage)
        }
    }

    private fun processMeteoriteStyleExplosion(
        world: ServerLevel,
        user: LivingEntity,
        target: Vec3,
        blockExplosionRadius: Int
    ) {
        val pendingExplosionBlocks = ArrayDeque<BlockPos>()
        val explosionBlocksPerTick = MeteoriteImpactHelper.queueImpactExplosion(
            pendingBlocks = pendingExplosionBlocks,
            radius = blockExplosionRadius,
            center = target,
            impactDir = IMPACT_DIRECTION
        )
        MeteoriteImpactHelper.processPendingExplosion(
            world = world,
            pendingBlocks = pendingExplosionBlocks,
            explosionBlocksPerTick = explosionBlocksPerTick,
            source = user
        )
        if (pendingExplosionBlocks.isEmpty()) {
            return
        }
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, IMPACT_BLOCK_TICK_BUDGET) {
            MeteoriteImpactHelper.processPendingExplosion(
                world = world,
                pendingBlocks = pendingExplosionBlocks,
                explosionBlocksPerTick = explosionBlocksPerTick,
                source = user
            )
        }.setCancelPredicate {
            pendingExplosionBlocks.isEmpty()
        }
    }


    private fun handleExplodeParticle(world: ServerLevel, user: LivingEntity, target: Vec3) {
        ServerCameraUtil.sendShake(world, target, 512.0, 10.0, 60, 240.0, true)
        var count = 0
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(10, 50) {
            ParticleEmittersManager.spawnEmitters(
                ExplosionMagicCloudEmitter(target, world).apply {
                    maxTick = 40
                    r = (++count) * 10.0
                }
            )
        }
        world.playSound(
            null,
            target.x,
            target.y,
            target.z,
            UsefulMagicSoundEvents.MAGIC_EXPLODE.get(),
            user.soundSource,
            32f,
            1f
        )

        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5) {
            world.playSound(
                null,
                target.x,
                target.y,
                target.z,
                SoundEvents.GENERIC_EXPLODE.value(),
                user.soundSource,
                32f, 0.8f
            )
        }
        // wave
        val explosion = ExplodeMagicEmitters(target.add(0.0, 5.0, 0.0), world).apply {
            this.templateData.also {
                it.size = 0.4f
            }
            randomParticleAgeMin = 60
            randomParticleAgeMax = 120
            precentDrag = 0.95
            maxTick = 4
            ballCountPow = 3 * 15
            minSpeed = 10.0
            maxSpeed = 40.0
            randomCountMin = 120 * 3
            randomCountMax = 400 * 3
            gravity = -0.001
        }
        ParticleEmittersManager.spawnEmitters(explosion)

        val explosionAnimate = ExplosionAnimateLaserMagicEmitters(target, world)
            .apply {
                maxTick = 2
                heightStep = 5.0
                minDiscrete = 5.0
                maxDiscrete = 15.0
                radiusStep = 2.0
                maxRadius = 20.0
                minCount = 20
                maxCount = 60
                templateData.maxAge = 80
                templateData.size = 0.3f
                templateData.effect = ControlableCloudEffect(templateData.uuid)
            }
        ParticleEmittersManager.spawnEmitters(explosionAnimate)

        fun genWave(yOffset: Double, speed: Double, drag: Double, size: Double): ExplosionWaveEmitters {
            return ExplosionWaveEmitters(target.add(0.0, yOffset, 0.0), world)
                .apply {
                    maxTick = 1
                    waveSpeed = speed
                    waveSize = size
                    waveCircleCountMax = 660
                    waveCircleCountMin = 120
                    speedDrag = drag
                    this.templateData.also {
                        it.effect = ControlableCloudEffect(it.uuid)
                        it.size = 1f
                        it.maxAge = 200
                        it.velocity = Vec3(0.0, 0.01, 0.0)
                    }
                    discrete = 0.1
                    randomVector = true
                    randomSpeed = 0.02
                }
        }

        // 冲击波云
        ParticleEmittersManager.spawnEmitters(genWave(20.0, 6.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(40.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(60.0, 15.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(80.0, 19.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(90.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(110.0, 18.0, 0.85, 1.0))



    }
}
