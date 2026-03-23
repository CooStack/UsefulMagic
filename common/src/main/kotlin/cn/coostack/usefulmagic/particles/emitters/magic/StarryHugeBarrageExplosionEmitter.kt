package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.*
import cn.coostack.cooparticlesapi.network.particle.emitters.command.*
import cn.coostack.cooparticlesapi.network.particle.emitters.command.curve.*
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.Supplier
import kotlin.math.*
import kotlin.random.Random

@CooAutoRegister
class StarryHugeBarrageExplosionEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    val cloud = ParticleCommandQueue()
        .add(
            ParticleToroidalCirculationCommand()
                .center{this.pos.add(0.0,15.0,0.0)}
                .axis(Vec3(0.0, 1.0, 0.0))
                .ringRadius(10.0)
                .radialThickness(8.0)
                .axialThickness(4.0)
                .circulationStrength(-3.0)
                .outwardStrength(2.0)
                .upwardStrength(0.5)
                .followStrength(1.5)
                .maxStep(2.0)
                .useLifeCurve(true)
        )
        .add(
            ParticleNoiseCommand()
                .strength(0.4)
                .frequency(3.0)
                .speed(5.0)
                .affectY(1.0)
                .clampSpeed(15.0)
                .useLifeCurve(true)
        )
        .add(
            ParticleDragCommand()
                .damping(0.15)
                .minSpeed(0.0)
                .linear(0.0)
        ) { data, particle ->
            (run { val age = particle.currentAge; val maxAge = particle.lifetime; ((age >= 10)) })
        }

    val wave = ParticleCommandQueue()
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
                .damping(0.05)
                .minSpeed(0.0)
                .linear(0.0)
        ) { data, particle ->
            (run { val age = particle.currentAge; val maxAge = particle.lifetime; ((age >= 10)) })
        }

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
                        1.0f + (0.301961f - 1.0f) * colorLifeProgress,
                        0.505882f + (0.188235f - 0.505882f) * colorLifeProgress,
                        0.239216f + (0.137255f - 0.239216f) * colorLifeProgress
                    )
                }
                1 -> {
                    this.color = Vector3f(
                        1.0f + (0.6f - 1.0f) * colorLifeProgress,
                        1.0f + (0.6f - 1.0f) * colorLifeProgress,
                        1.0f + (0.6f - 1.0f) * colorLifeProgress
                    )
                }
            }
            if (data.sign == 0) {
                cloud.applyVelocity(data, this)
            }
            if (data.sign == 1) {
                wave.applyVelocity(data, this)
            }
        }
    }

    init {
        delay = 1
        maxTick = -1
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 40
                    maxAge = 50
                    minCount = 100
                    maxCount = 150
                    minSize = 0.4
                    maxSize = 1.2
                    minSpeed = 1.0
                    maxSpeed = 3.0
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.15, 0.0)
                    visibleRange = 128.0f
                    color = Vector3f(1.0f, 0.505882f, 0.239216f)
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
                                val rr = 2.0
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
                                val baseDir = Vec3(0.0, 0.15, 0.0)
                                velocity = if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        // 发射器 #2: Emitter 2
        if (tick >= 0 && tick <= 3) {
            run {
                val data2 = SimpleRandomParticleData().apply {
                    minAge = 10
                    maxAge = 20
                    minCount = 120
                    maxCount = 240
                    minSize = 0.8
                    maxSize = 1.2
                    minSpeed = 4.0
                    maxSpeed = 5.0
                }

                val template2 = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 128.0f
                    color = Vector3f(1.0f, 1.0f, 1.0f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 1
                }

                template2.apply {
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addDiscreteCircleXZ(0.4, data2.getRandomCount(), 0.15)
                        .rotateTo(RelativeLocation(0.0, 1.0, 0.0))
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data2.getRandomSpeed()
                            template2.clone().apply {
                                maxAge = data2.getRandomParticleMaxAge()
                                size = data2.getRandomSize()
                                val dir = rel.toVector()
                                velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO else dir.normalize().scale(speed)
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