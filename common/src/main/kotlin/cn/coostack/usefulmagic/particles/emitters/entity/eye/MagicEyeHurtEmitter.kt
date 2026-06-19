package cn.coostack.usefulmagic.particles.emitters.entity.eye

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random

@CooAutoRegister
class MagicEyeHurtEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    private fun buildExplodeQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleNoiseCommand()
                    .strength(0.15)
                    .frequency(0.6)
                    .speed(0.12)
                    .affectY(1.0)
                    .clampSpeed(0.8)
                    .useLifeCurve(true)
            )
            .add(
                ParticleDragCommand()
                    .damping(0.15)
                    .minSpeed(0.0)
                    .linear(0.0)
            )
    }
    val particleOption = SimpleRandomParticleData().apply {
        minAge = 5
        maxAge = 15
        minCount = 60
        maxCount = 80
        minSize = 0.1
        maxSize = 0.3
        minSpeed = 1.0
        maxSpeed = 1.2
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        var explode: ParticleCommandQueue? = null
        controler.addPreTickAction {
            val colorLifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        0.847059f + (0.980392f - 0.847059f) * colorLifeProgress,
                        0.384314f + (0.086275f - 0.384314f) * colorLifeProgress,
                        0.976471f + (0.996078f - 0.976471f) * colorLifeProgress
                    )
                }
            }
            val queue = explode ?: buildExplodeQueue().also { explode = it }
            queue.applyVelocity(data, this)
        }
    }

    init {
        delay = 1
        maxTick = 1
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {


                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.0, 0.0)
                    visibleRange = 128.0f
                    color = Vector3f(0.847059f, 0.384314f, 0.976471f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 0
                }

                template1.apply {
                    effect = ControlableEnchantmentEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = particleOption.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * PI * u
                                val phi = acos(2.0 * v - 1.0)
                                val dx = sin(phi) * cos(theta)
                                val dy = cos(phi)
                                val dz = sin(phi) * sin(theta)
                                val rr = 0.5 * cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = particleOption.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = particleOption.getRandomParticleMaxAge()
                                size = particleOption.getRandomSize()
                                val baseDir = Vec3(0.0, 0.0, 0.0)
                                velocity =
                                    if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        return res
    }

    override fun doTick() {
        // modify emitter variables here
    }
}
