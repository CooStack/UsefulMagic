package cn.coostack.usefulmagic.particles.emitters.entity.eye

import cn.coostack.cooparticlesapi.annotations.CodecField
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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

@CooAutoRegister
class MagicThornEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    val explode = ParticleCommandQueue()
        .add(
            ParticleNoiseCommand()
                .strength(0.15)
                .frequency(0.6)
                .speed(0.12)
                .affectY(1.0)
                .clampSpeed(2.0)
                .useLifeCurve(true)
        )
        .add(
            ParticleDragCommand()
                .damping(0.15)
                .minSpeed(0.0)
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
                        0.847059f + (0.980392f - 0.847059f) * colorLifeProgress,
                        0.384314f + (0.086275f - 0.384314f) * colorLifeProgress,
                        0.976471f + (0.996078f - 0.976471f) * colorLifeProgress
                    )
                }
            }
            explode.applyVelocity(data, this)
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
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 5
                    maxAge = 15
                    minCount = 60
                    maxCount = 80
                    minSize = 0.1
                    maxSize = 0.3
                    minSpeed = 0.5
                    maxSpeed = 2.0
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 1.0, 0.0)
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
                            val count = data1.getRandomCount()
                            val density = 0.0
                            val surface = false
                            val pw = 1.0 + 3.0 * density
                            repeat(count) {
                                val u0x = rand.nextDouble() - 0.5
                                var x = (if (u0x < 0.0) -1.0 else 1.0) * abs(u0x).pow(pw) * (size).toDouble()
                                val u0y = rand.nextDouble() - 0.5
                                var y = (if (u0y < 0.0) -1.0 else 1.0) * abs(u0y).pow(pw) * 1.0
                                val u0z = rand.nextDouble() - 0.5
                                var z = (if (u0z < 0.0) -1.0 else 1.0) * abs(u0z).pow(pw) * (size).toDouble()
                                if (surface) {
                                    when (rand.nextInt(3)) {
                                        0 -> x = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * (size).toDouble()
                                        1 -> y = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * 1.0
                                        2 -> z = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * (size).toDouble()
                                    }
                                }
                                locs.add(RelativeLocation(x + 0.0, y + 0.0, z + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data1.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = data1.getRandomParticleMaxAge()
                                size = data1.getRandomSize()
                                val baseDir = Vec3(0.0, 1.0, 0.0)
                                velocity = if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        return res
    }

    @CodecField
    var size: Double = 4.0

    private fun applyEmitterVarBounds() {
        size = size.coerceIn(3.0, 5.0)
    }

    override fun doTick() {
        // modify emitter variables here
    }
}