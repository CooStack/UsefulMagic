package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.*
import cn.coostack.cooparticlesapi.network.particle.emitters.command.*
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random

@CooAutoRegister
class StarryMeteoriteSplitEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    val command1 = ParticleCommandQueue()
        .add(
            ParticleNoiseCommand()
                .strength(0.6)
                .frequency(0.15)
                .speed(0.12)
                .affectY(1.0)
                .clampSpeed(12.0)
                .useLifeCurve(true)
        )
        .add(
            ParticleDragCommand()
                .damping(0.1)
                .minSpeed(0.05)
                .linear(0.0)
        )

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        controler.addPreTickAction {
            val colorLifeProgress = if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        0.94902f + (0.301961f - 0.94902f) * colorLifeProgress,
                        0.305882f + (0.0f - 0.305882f) * colorLifeProgress,
                        0.027451f + (0.031373f - 0.027451f) * colorLifeProgress
                    )
                }
            }
            command1.applyVelocity(data, this)
        }
    }

    init {
        delay = 1
        maxTick = -1
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0 && tick <= 2) {
            run {
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 10
                    maxAge = 50
                    minCount = 200
                    maxCount = 500
                    minSize = 0.5
                    maxSize = 0.85
                    minSpeed = 6.0
                    maxSpeed = 7.0
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 128.0f
                    color = Vector3f(0.94902f, 0.305882f, 0.027451f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 0
                }

                template1.apply {
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = data1.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * PI * u
                                val phi = acos(2.0 * v - 1.0)
                                val dx = sin(phi) * cos(theta)
                                val dy = cos(phi)
                                val dz = sin(phi) * sin(theta)
                                val rr = 1.0 * cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data1.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = data1.getRandomParticleMaxAge()
                                size = data1.getRandomSize()
                                val dir = rel.toVector()
                                velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO else dir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        return res
    }

    @CodecField
    var direction: Vec3 = Vec3(0.0, 1.0, 0.0)

    override fun doTick() {
    }
}