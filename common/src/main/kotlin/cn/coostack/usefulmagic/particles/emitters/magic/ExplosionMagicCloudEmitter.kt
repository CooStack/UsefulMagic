package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.plus
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
class ExplosionMagicCloudEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var r = 10.0
    val command1 = ParticleCommandQueue()
        .add(
            ParticleNoiseCommand()
                .strength(0.03)
                .frequency(0.15)
                .speed(0.12)
                .affectY(1.0)
                .clampSpeed(4.0)
                .useLifeCurve(true)
        )
        .add(
            ParticleToroidalCirculationCommand()
                .center { this.pos.add(0.0, 15.0, 0.0) }
                .axis(Vec3(0.0, 1.0, 0.0))
                .ringRadius(r)
                .radialThickness(13.0)
                .axialThickness(8.0)
                .circulationStrength(-1.0)
                .outwardStrength(2.0)
                .upwardStrength(1.0)
                .followStrength(1.5)
                .maxStep(1.5)
                .useLifeCurve(true)
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
            val colorLifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        0.996078f + (1.0f - 0.996078f) * colorLifeProgress,
                        0.607843f + (0.039216f - 0.607843f) * colorLifeProgress,
                        0.062745f + (0.039216f - 0.062745f) * colorLifeProgress
                    )
                }
            }
            command1.applyVelocity(data, this)
        }
    }

    init {
        delay = 1
        maxTick = 30
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 40
                    maxAge = 80
                    minCount = 120
                    maxCount = 140
                    minSize = 0.8
                    maxSize = 1.8
                    minSpeed = 1.0
                    maxSpeed = 1.2
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.15, 0.0)
                    visibleRange = 512.0f
                    color = Vector3f(0.996078f, 0.607843f, 0.062745f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 0
                }

                template1.apply {
                    effect = ControlableEndRodEffect(uuid)
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
                                val rr = 2.0 * cbrt(rand.nextDouble())
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
        if (tick < 70 && tick % 2 == 0) {
            pos += Vec3(0.0, 2.0, 0.0)
        }
    }
}