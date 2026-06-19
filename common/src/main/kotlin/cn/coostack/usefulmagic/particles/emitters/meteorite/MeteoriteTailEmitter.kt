package cn.coostack.usefulmagic.particles.emitters.meteorite

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.control.RemoveReason
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.sin

@CooAutoRegister
class MeteoriteTailEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var mainData = SimpleRandomParticleData().apply {
        minAge = 20
        maxAge = 40
        minCount = 50
        maxCount = 100
        minSize = 0.1
        maxSize = 0.3
        minSpeed = 1.0
        maxSpeed = 1.5
    }

    init {
        delay = 1
        maxTick = -1
        enableInterpolator = true
        emittersInterpolator.setRefiner(1.25)
    }

    private fun buildFireCommand(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleNoiseCommand()
                    .strength(0.03)
                    .frequency(0.15)
                    .speed(0.12)
                    .affectY(1.0)
                    .clampSpeed(0.8)
                    .useLifeCurve(true)
            )
            .add(
                ParticleDragCommand()
                    .damping(0.01)
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
        var fireCommand: ParticleCommandQueue? = null
        controler.addPreTickAction {
            val queue = fireCommand ?: buildFireCommand().also { fireCommand = it }
            queue.applyVelocity(data, this)
            if (data.sign == 1) {
                val progress = currentAge / lifetime.toDouble()
                color = GraphMathHelper.lerp(progress, Vector3f(1f, 1f, 1f), Vector3f(1F, 79 / 255F, 66 / 255F))
            }
        }
    }

    @CodecField
    var radius: Double = 2.0

    @CodecField
    var direction: Vec3 = Vec3(0.0, 0.0, 1.0)
    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {
                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.0, 1.0)
                    visibleRange = 512.0f
                    color = Vector3f(1.0f, 1.0f, 1.0f)
                    setTextureSheet(CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT)
                    sign = 1
                }


                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = kotlin.random.Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = mainData.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * Math.PI * u
                                val phi = Math.acos(2.0 * v - 1.0)
                                val dx = Math.sin(phi) * Math.cos(theta)
                                val dy = Math.cos(phi)
                                val dz = Math.sin(phi) * Math.sin(theta)
                                val rr = radius * Math.cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = mainData.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = mainData.getRandomParticleMaxAge()
                                size = mainData.getRandomSize().toFloat()
                                val baseDir = direction
                                velocity =
                                    if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        // 发射器 #2: Emitter 2
        if (tick >= 0) {
            run {
                val data2 = SimpleRandomParticleData().apply {
                    minAge = 30
                    maxAge = 90
                    minCount = 40
                    maxCount = 60
                    minSize = 0.1
                    maxSize = 0.3
                    minSpeed = 2.0
                    maxSpeed = 3.0
                }

                val template2 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.0, 1.0)
                    visibleRange = 512.0f
                    color = Vector3f(0.439216f, 0.439216f, 0.439216f)
                    alpha = 1.0f
                    light = -1
                    setTextureSheet(CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT)
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = kotlin.random.Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = data2.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * Math.PI * u
                                val phi = Math.acos(2.0 * v - 1.0)
                                val dx = Math.sin(phi) * Math.cos(theta)
                                val dy = Math.cos(phi)
                                val dz = Math.sin(phi) * Math.sin(theta)
                                val rr = radius * Math.cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data2.getRandomSpeed()
                            template2.clone().apply {
                                maxAge = data2.getRandomParticleMaxAge()
                                size = data2.getRandomSize()
                                val baseDir = direction
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

    }
}
