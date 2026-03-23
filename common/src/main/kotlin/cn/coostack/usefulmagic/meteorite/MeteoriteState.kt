package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.extend.minus
import cn.coostack.usefulmagic.extend.plus
import cn.coostack.usefulmagic.particles.emitters.magic.MeteoriteExplosionEmitter
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.random.Random

object MeteoriteState {
    private const val METEOR_SOUND_RANGE = 512.0
    private const val METEOR_NEAR_DISTANCE = 12.0
    private const val EXPLOSION_RADIUS_MULTIPLIER = 1.25
    private const val ENTITY_DAMAGE_RADIUS_MULTIPLIER = 2.0

    val FALLING = object : MeteoriteStateAction {
        override fun tick(context: MeteoriteAnimateAction) {
            processPendingExplosion(context)
            val mainBarrage = context.mainBarrage ?: return
            if (mainBarrage.mergeCount <= 0 && mainBarrage.freeze) {
                if (context.beforeFallingTick++ > 80) {
                    val dir = context.fallingTarget - context.mainSpawnPos
                    mainBarrage.direction = dir
                    mainBarrage.options.enableSpeedWithOptions(
                        context.fallingSpeed
                    ).maxAcrossCount(4)
                    playMeteorFarFlySound(context, mainBarrage.loc)
                    mainBarrage.freeze = false
                    context.tailEmitter.direction = -dir
                    context.barrageMagicComposition.remove()
                    ParticleEmittersManager.spawnEmitters(context.tailEmitter)
                    mainBarrage.addPreTickAction {
                        context.tailEmitter.teleportTo(loc)
                        context.tailEmitter.direction = -dir
                    }.addHitOnServer {
                        if (context.impactTriggered) return@addHitOnServer
                        val fallDir = dir.normalize()
                        stopMeteorFarFlySound(context, loc)
                        playMeteorImpactSound(context, loc)
                        val explosionCenter = loc.add(fallDir * (context.targetSize * 1.25))
                        val explosionRadius = (exp(context.explosionPower) * EXPLOSION_RADIUS_MULTIPLIER)
                            .roundToInt()
                            .coerceAtLeast(1)
                        val entityDamageRadius = explosionRadius * ENTITY_DAMAGE_RADIUS_MULTIPLIER
                        queueMeteorImpactExplosion(
                            context = context,
                            radius = explosionRadius,
                            center = explosionCenter,
                            impactDir = fallDir
                        )
                        applyMeteoriteEntityDamage(
                            context = context,
                            center = explosionCenter,
                            source = shooter!!,
                            spellDamage = context.spellDamage,
                            radius = entityDamageRadius,
                            fullDamageRadius = context.targetSize.toDouble()
                        )
                        context.tailEmitter.cancelled = true
                        ServerCameraUtil.sendShake(context.world, loc, 256.0, 5.0, 20, 240.0, false)
                        context.world.players().filter {
                            it.position().distanceTo(loc) <= 256.0
                                    && it.canSee(loc)
                        }
                            .forEach {
                                CooPostEffects.server.send(it, UsefulMagicPostEffects.flameExplodeFlash(40, 2f, 2f))
                            }

                        ParticleEmittersManager.spawnEmitters(
                            MeteoriteExplosionEmitter(
                                loc + dir.normalize() * 10,
                                world
                            )
                        )
                        processPendingExplosion(context)

                        context.fallingFinished = !context.hasPendingExplosion()
                    }
                }
            }
            if (!mainBarrage.freeze && !context.impactTriggered) {
                ServerCameraUtil.sendShake(context.world, mainBarrage.loc, 256.0, 4.0, 10, 100.0, true)
                tryPlayMeteorNearFlySound(context, mainBarrage.loc)
            }
            if (context.mainBarrage!!.tick > 1000 && context.mainBarrage!!.valid) {
                context.mainBarrage!!.remove()
                context.fallingFinished = true
            }
        }

        override fun canNext(context: MeteoriteAnimateAction): Boolean {
            return context.fallingFinished && !context.hasPendingExplosion()
        }

        override fun next(context: MeteoriteAnimateAction) {
            context.done = true
        }

        override fun start(context: MeteoriteAnimateAction) {
            context.fallingFinished = false
            context.beforeFallingTick = 0
            context.farFlySoundStarted = false
            context.nearFlySoundPlayed = false
            context.impactTriggered = false
            context.pendingExplosionBlocks.clear()
        }
    }

    val MERGING = object : MeteoriteStateAction {
        override fun tick(context: MeteoriteAnimateAction) {
            val mainBarrage = context.mainBarrage ?: return
            mainBarrage.targetSize.speed = 2.0
            val count = context.summingCount.random()
            repeat(count) {
                val size = context.targetSize / (context.mergingTotalTick * count)
                val minSpawnDistance = context.targetSize.toDouble() + 24.0
                val maxSpawnDistance = context.targetSize.toDouble() + 48.0
                val spawnPos = context.mainSpawnPos + Vec3.ZERO.random() * Random.nextDouble(
                    minSpawnDistance,
                    maxSpawnDistance
                )
                var mergeIncreaseScaled = size
                val barrageToMerge =
                    MeteoriteBarrage(
                        spawnPos, context.world, MeteoriteDisplay(spawnPos, context.world).apply {
                            state = Blocks.NETHERRACK.defaultBlockState()
                            scale = 0f
                            prevScale = 0f
                        },
                        BarrageOption()
                            .acrossBlock(true)
                            .acrossLiquid(true)
                            .acrossable(true)
                    )
                barrageToMerge.mergeSign = true
                barrageToMerge.targetSize.targetNum = size + 1F
                barrageToMerge.addPreTickAction {
                    val next = PhysicsUtil.nextAttractVelocityNullable(
                        loc, direction.normalize() * options.speed,
                        mainBarrage.loc,
                        falloffPow = 1,
                        strength = 5.0,
                        maxSpeed = 12.0,
                        arriveRadius = mainBarrage.targetSize.targetNum.toDouble()
                    ) ?: let {
                        remove()
                        mainBarrage.mergeCount--
                        mainBarrage.targetSize.targetNum += mergeIncreaseScaled
                        return@addPreTickAction
                    }
                    direction = next.normalize()
                    options.speed(next.length())
                }
                BarrageManager.spawn(barrageToMerge)
                mainBarrage.mergeCount++
            }
            context.mergingTick++
        }

        override fun canNext(context: MeteoriteAnimateAction): Boolean {
            return context.mergingTick >= context.mergingTotalTick
        }

        override fun next(context: MeteoriteAnimateAction) {
            context.barrageState = FALLING
        }

        override fun start(context: MeteoriteAnimateAction) {
            context.mergingTick = 0
            val mergeSpeed = context.mergeSpeed.coerceAtLeast(0.001f)
            context.mergingTotalTick = (context.targetSize / mergeSpeed).roundToInt().coerceAtLeast(1)
        }
    }

    val START = object : MeteoriteStateAction {
        override fun tick(context: MeteoriteAnimateAction) {
        }

        override fun canNext(context: MeteoriteAnimateAction): Boolean {
            return context.mainBarrage != null
        }

        override fun next(context: MeteoriteAnimateAction) {
            context.barrageState = MERGING
        }

        override fun start(context: MeteoriteAnimateAction) {
            val mainBarrage = MeteoriteBarrage(
                context.mainSpawnPos, context.world, MeteoriteDisplay(context.mainSpawnPos, context.world).apply {
                    scale = 0f
                    prevScale = 0f
                },
                BarrageOption()
            )
            mainBarrage.shooter = context.shooter
            mainBarrage.freeze = true
            mainBarrage.targetSize.speed = 0.2
            mainBarrage.targetSize.targetNum -= mainBarrage.targetSize.targetNum
            context.mainBarrage = mainBarrage
            BarrageManager.spawn(mainBarrage)
        }
    }

    private fun queueMeteorImpactExplosion(
        context: MeteoriteAnimateAction,
        radius: Int,
        center: Vec3,
        impactDir: Vec3
    ) {
        context.impactTriggered = true
        context.explosionBlocksPerTick = MeteoriteImpactHelper.queueImpactExplosion(
            pendingBlocks = context.pendingExplosionBlocks,
            radius = radius,
            center = center,
            impactDir = impactDir
        )
        if (!context.hasPendingExplosion()) {
            context.fallingFinished = true
        }
    }

    private fun processPendingExplosion(context: MeteoriteAnimateAction) {
        if (!context.impactTriggered || !context.hasPendingExplosion()) return
        MeteoriteImpactHelper.processPendingExplosion(
            world = context.world,
            pendingBlocks = context.pendingExplosionBlocks,
            explosionBlocksPerTick = context.explosionBlocksPerTick,
            source = context.shooter
        )
        if (!context.hasPendingExplosion()) {
            context.fallingFinished = true
        }
    }

    private fun applyMeteoriteEntityDamage(
        context: MeteoriteAnimateAction,
        center: Vec3,
        source: LivingEntity,
        spellDamage: Float,
        radius: Double,
        fullDamageRadius: Double
    ) {
        if (spellDamage <= 0f || radius <= 0.0) return
        val effectiveFullDamageRadius = fullDamageRadius.coerceIn(0.0, radius)
        val world = context.world
        val rangeBox = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0)
        val damageSource = UsefulMagicDamageSources.entityMagic(world, source, source)
        world.getEntitiesOfClass(LivingEntity::class.java, rangeBox) { target ->
            target.isAlive && target.uuid != source.uuid && FriendFilterHelper.filterNotFriend(source, target)
        }.forEach { target ->
            val targetPoint = target.boundingBox.center
            val distance = targetPoint.distanceTo(center)
            if (distance > radius) return@forEach

            val distanceDamageFactor = if (
                distance <= effectiveFullDamageRadius || effectiveFullDamageRadius >= radius
            ) {
                1.0f
            } else {
                val falloffRate = (
                        (distance - effectiveFullDamageRadius) / (radius - effectiveFullDamageRadius)
                        ).coerceIn(0.0, 1.0).toFloat()
                1.0f - falloffRate
            }
            val blockFactor = calculateBlockDamageFactor(world = world, start = center, end = targetPoint)
            if (blockFactor <= 0f) return@forEach

            val finalDamage = spellDamage * distanceDamageFactor * blockFactor
            if (finalDamage <= 0.05f) return@forEach

            target.hurt(damageSource, finalDamage)
        }

    }

    private fun calculateBlockDamageFactor(
        world: ServerLevel,
        start: Vec3,
        end: Vec3
    ): Float {
        val lineDistance = start.distanceTo(end)
        if (lineDistance <= 1e-6) return 1f

        val sampleCount = (lineDistance * 2.0).roundToInt().coerceAtLeast(1)
        val visited = HashSet<BlockPos>(sampleCount + 1)
        var factor = 1f
        for (i in 0..sampleCount) {
            val t = i.toDouble() / sampleCount
            val point = start.lerp(end, t)
            val pos = BlockPos.containing(point)
            if (!visited.add(pos)) continue

            val state = world.getBlockState(pos)
            if (state.isAir || state.getCollisionShape(world, pos).isEmpty) continue

            factor *= 0.9f
        }
        return factor.coerceIn(0f, 1f)
    }

    private fun playMeteorFarFlySound(context: MeteoriteAnimateAction, soundPos: Vec3) {
        if (context.farFlySoundStarted) return
        context.world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_FALL_FAR.get(),
            SoundSource.HOSTILE,
            16f,
            1f
        )
        context.farFlySoundStarted = true
    }

    private fun tryPlayMeteorNearFlySound(context: MeteoriteAnimateAction, soundPos: Vec3) {
        if (context.nearFlySoundPlayed) return
        val distanceToGroundTarget = soundPos.distanceTo(context.fallingTarget)
        if (distanceToGroundTarget > METEOR_NEAR_DISTANCE) return
        stopMeteorFarFlySound(context, soundPos)
        context.world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_FALL_NEAR.get(),
            SoundSource.HOSTILE,
            18f,
            1.1f
        )
        context.nearFlySoundPlayed = true
    }

    private fun playMeteorImpactSound(context: MeteoriteAnimateAction, soundPos: Vec3) {
        context.world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_IMPACT.get(),
            SoundSource.HOSTILE,
            32f,
            1f
        )
    }

    private fun stopMeteorFarFlySound(context: MeteoriteAnimateAction, soundPos: Vec3) {
        if (!context.farFlySoundStarted) return
        val stopPacket = ClientboundStopSoundPacket(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "meteor_fall_far"),
            SoundSource.HOSTILE
        )
        nearbyPlayers(context.world, soundPos, METEOR_SOUND_RANGE).forEach {
            it.connection.send(stopPacket)
        }
        context.farFlySoundStarted = false
    }

    private fun nearbyPlayers(world: ServerLevel, center: Vec3, range: Double): List<ServerPlayer> {
        val rangeSqr = range * range
        return world.players().filter { it.position().distanceToSqr(center) <= rangeSqr }
    }
}
