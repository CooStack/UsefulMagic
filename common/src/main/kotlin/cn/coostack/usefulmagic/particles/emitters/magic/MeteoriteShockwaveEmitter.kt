package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.random.Random

@CooAutoRegister
class MeteoriteShockwaveEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var effectScale = 1.0

    init {
        delay = 1
        maxTick = 8
    }

    private fun scaleFactor(): Double = effectScale.coerceAtLeast(0.25)

    private fun scaledDistance(value: Double): Double = value * scaleFactor()

    private fun scaledMotion(value: Double): Double = value * scaleFactor()

    // 粒子的视觉大小: 上限不再放大, 下限随 effectScale 一同缩小
    private fun scaledSize(value: Double): Double = value * scaleFactor().coerceAtMost(1.0)

    private fun buildRingQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleDragCommand()
                    .damping(0.06)
                    .minSpeed(0.05)
                    .linear(0.0)
            )
    }

    private fun buildDustQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleDragCommand()
                    .damping(0.12)
                    .minSpeed(0.0)
                    .linear(0.0)
            )
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        var ring: ParticleCommandQueue? = null
        var dust: ParticleCommandQueue? = null
        controler.addPreTickAction {
            val lifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                // 主冲击波环
                0 -> {
                    this.color = Vector3f(
                        1.0f + (0.45f - 1.0f) * lifeProgress,
                        0.78f + (0.42f - 0.78f) * lifeProgress,
                        0.36f + (0.40f - 0.36f) * lifeProgress
                    )
                    val queue = ring ?: buildRingQueue().also { ring = it }
                    queue.applyVelocity(data, this)
                }
                // 扬尘
                1 -> {
                    this.color = Vector3f(
                        0.92f + (0.50f - 0.92f) * lifeProgress,
                        0.86f + (0.44f - 0.86f) * lifeProgress,
                        0.74f + (0.38f - 0.74f) * lifeProgress
                    )
                    val queue = dust ?: buildDustQueue().also { dust = it }
                    queue.applyVelocity(data, this)
                }
            }
        }
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: 冲击波主环
        if (tick in 0..2) {
            run {
                val ringData = SimpleRandomParticleData().apply {
                    minAge = 20
                    maxAge = 32
                    minCount = 160
                    maxCount = 240
                    minSize = scaledSize(3.0)
                    maxSize = scaledSize(4.5)
                    minSpeed = scaledMotion(2.4)
                    maxSpeed = scaledMotion(3.6)
                }

                val template = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 512.0f
                    color = Vector3f(1.0f, 0.78f, 0.36f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 0
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addDiscreteCircleXZ(
                            scaledDistance(1.0),
                            ringData.getRandomCount(),
                            0.15
                        )
                        .createWithoutClone()
                        .map { rel ->
                            val speed = ringData.getRandomSpeed()
                            template.clone().apply {
                                maxAge = ringData.getRandomParticleMaxAge()
                                size = ringData.getRandomSize()
                                val dir = rel.toVector()
                                velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO
                                else dir.normalize().scale(speed).add(0.0, scaledMotion(0.08), 0.0)
                            } to rel
                        }
                )
            }
        }

        // 发射器 #2: 扬尘
        if (tick in 0..3) {
            run {
                val dustData = SimpleRandomParticleData().apply {
                    minAge = 30
                    maxAge = 60
                    minCount = 80
                    maxCount = 140
                    minSize = scaledSize(2.0)
                    maxSize = scaledSize(3.5)
                    minSpeed = scaledMotion(1.0)
                    maxSpeed = scaledMotion(2.0)
                }

                val template = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 512.0f
                    color = Vector3f(0.92f, 0.86f, 0.74f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 1
                    effect = ControlableCloudEffect(uuid)
                }

                val rand = Random.Default
                res.addAll(
                    PointsBuilder()
                        .addDiscreteCircleXZ(
                            scaledDistance(0.6),
                            dustData.getRandomCount(),
                            0.4
                        )
                        .createWithoutClone()
                        .map { rel ->
                            val speed = dustData.getRandomSpeed()
                            val lift = scaledDistance(rand.nextDouble(0.0, 0.6))
                            val biased = RelativeLocation(rel.x, rel.y + lift, rel.z)
                            template.clone().apply {
                                maxAge = dustData.getRandomParticleMaxAge()
                                size = dustData.getRandomSize()
                                val dir = rel.toVector()
                                velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO
                                else dir.normalize().scale(speed)
                                    .add(0.0, scaledMotion(rand.nextDouble(0.3, 0.8)), 0.0)
                            } to biased
                        }
                )
            }
        }

        return res
    }

    override fun doTick() {
    }
}
