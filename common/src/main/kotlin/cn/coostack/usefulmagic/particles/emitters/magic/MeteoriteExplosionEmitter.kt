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
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.Supplier
import kotlin.math.*
import kotlin.random.Random

@CooAutoRegister
class MeteoriteExplosionEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var effectScale = 1.0

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        var sp1: ParticleCommandQueue? = null
        var sp2: ParticleCommandQueue? = null
        var sp2Drag: ParticleCommandQueue? = null
        var wave: ParticleCommandQueue? = null
        controler.addPreTickAction {
            val colorLifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        1.0f + (0.439216f - 1.0f) * colorLifeProgress,
                        0.847059f + (0.439216f - 0.847059f) * colorLifeProgress,
                        0.419608f + (0.439216f - 0.419608f) * colorLifeProgress
                    )
                }

                1 -> {
                    this.color = Vector3f(
                        0.92549f + (0.431373f - 0.92549f) * colorLifeProgress,
                        0.482353f + (0.431373f - 0.482353f) * colorLifeProgress,
                        0.294118f + (0.431373f - 0.294118f) * colorLifeProgress
                    )
                }

                2 -> {
                    this.color = Vector3f(
                        1.0f + (0.639216f - 1.0f) * colorLifeProgress,
                        1.0f + (0.639216f - 1.0f) * colorLifeProgress,
                        1.0f + (0.639216f - 1.0f) * colorLifeProgress
                    )
                }
            }
            if (data.sign == 0) {
                val queue = sp1 ?: buildSp1Queue().also { sp1 = it }
                queue.applyVelocity(data, this)
            }
            if (data.sign == 1) {
                if (currentAge >= 10) {
                    val dragQueue = sp2Drag ?: buildSp2DragQueue().also { sp2Drag = it }
                    dragQueue.applyVelocity(data, this)
                }
                val queue = sp2 ?: buildSp2Queue().also { sp2 = it }
                queue.applyVelocity(data, this)
            }
            if (data.sign == 2) {
                val queue = wave ?: buildWaveQueue().also { wave = it }
                queue.applyVelocity(data, this)
            }
        }
    }

    init {
        delay = 1
        maxTick = 25
    }

    private fun scaleFactor(): Double = effectScale.coerceAtLeast(0.25)

    private fun scaledDistance(value: Double): Double = value * scaleFactor()

    private fun scaledMotion(value: Double): Double = value * scaleFactor()

    private fun buildSp1Queue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleToroidalCirculationCommand()
                    .center { this.pos.add(0.0, scaledDistance(50.0), 0.0) }
                    .axis(Vec3(0.0, 1.0, 0.0))
                    .ringRadius(scaledDistance(60.0))
                    .radialThickness(scaledDistance(20.0))
                    .axialThickness(scaledDistance(20.0))
                    .circulationStrength(scaledMotion(-5.0))
                    .outwardStrength(scaledMotion(-2.0))
                    .upwardStrength(0.0)
                    .followStrength(scaledMotion(8.0))
                    .maxStep(scaledDistance(1.0))
                    .useLifeCurve(false)
            )
    }

    private fun buildSp2Queue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleToroidalCirculationCommand()
                    .center { this.pos.add(0.0, 0.0, 0.0) }
                    .axis(Vec3(0.0, 1.0, 0.0))
                    .ringRadius(scaledDistance(30.0))
                    .radialThickness(scaledDistance(5.0))
                    .axialThickness(scaledDistance(5.0))
                    .circulationStrength(scaledMotion(-8.0))
                    .outwardStrength(scaledMotion(5.0))
                    .upwardStrength(scaledMotion(3.0))
                    .followStrength(scaledMotion(10.0))
                    .maxStep(scaledDistance(2.0))
                    .useLifeCurve(false)
            )
    }

    private fun buildSp2DragQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleDragCommand()
                    .damping(0.15)
                    .minSpeed(0.0)
                    .linear(0.0)
            )
    }

    private fun buildWaveQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleDragCommand()
                    .damping(0.05)
                    .minSpeed(0.1)
                    .linear(0.0)
            )
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 40
                    maxAge = 80
                    minCount = 100
                    maxCount = 200
                    minSize = 3.0
                    maxSize = 5.0
                    minSpeed = scaledMotion(5.0)
                    maxSpeed = scaledMotion(6.0)
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, scaledMotion(0.15), 0.0)
                    visibleRange = 512.0f
                    color = Vector3f(1.0f, 0.847059f, 0.419608f)
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
                                val rr = scaledDistance(5.0) * cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + scaledDistance(20.0), dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data1.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = data1.getRandomParticleMaxAge()
                                size = data1.getRandomSize()
                                val baseDir = Vec3(0.0, scaledMotion(0.15), 0.0)
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
                    minAge = 80
                    maxAge = 120
                    minCount = 20
                    maxCount = 60
                    minSize = 4.0
                    maxSize = 5.0
                    minSpeed = scaledMotion(5.0)
                    maxSpeed = scaledMotion(6.0)
                }

                val template2 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, scaledMotion(0.15), 0.0)
                    visibleRange = 512.0f
                    color = Vector3f(0.92549f, 0.482353f, 0.294118f)
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
                        .addWith {
                            val rand = Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = data2.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * PI * u
                                val phi = acos(2.0 * v - 1.0)
                                val dx = sin(phi) * cos(theta)
                                val dy = cos(phi)
                                val dz = sin(phi) * sin(theta)
                                val rr = scaledDistance(5.0) * cbrt(rand.nextDouble())
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
                                val baseDir = Vec3(0.0, scaledMotion(0.15), 0.0)
                                velocity =
                                    if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        // 发射器 #3: Emitter 3
        if (tick >= 0 && tick <= 5) {
            run {
                val data3 = SimpleRandomParticleData().apply {
                    minAge = 5
                    maxAge = 10
                    minCount = 500
                    maxCount = 600
                    minSize = 4.0
                    maxSize = 5.0
                    minSpeed = scaledMotion(20.0)
                    maxSpeed = scaledMotion(30.0)
                }

                val template3 = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 512.0f
                    color = Vector3f(1.0f, 1.0f, 1.0f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 3
                }

                template3.apply {
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = data3.getRandomCount()
                            repeat(count) {
                                val u = rand.nextDouble()
                                val v = rand.nextDouble()
                                val theta = 2.0 * PI * u
                                val phi = acos(2.0 * v - 1.0)
                                val dx = sin(phi) * cos(theta)
                                val dy = cos(phi)
                                val dz = sin(phi) * sin(theta)
                                val rr = scaledDistance(2.0) * cbrt(rand.nextDouble())
                                locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data3.getRandomSpeed()
                            template3.clone().apply {
                                maxAge = data3.getRandomParticleMaxAge()
                                size = data3.getRandomSize()
                                val dir = rel.toVector()
                                velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO else dir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        // 发射器 #4: Emitter 4
        if (tick in 0..5) {
            run {
                val waveData = SimpleRandomParticleData().apply {
                    minAge = 40
                    maxAge = 100
                    minCount = 300
                    maxCount = 500
                    minSize = 2.0
                    maxSize = 4.0
                    minSpeed = scaledMotion(5.0)
                    maxSpeed = scaledMotion(6.0)
                }

                val wave = ControlableParticleData().apply {
                    velocity = Vec3.ZERO
                    visibleRange = 512.0f
                    color = Vector3f(1.0f, 1.0f, 1.0f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 2
                }

                wave.apply {
                    effect = ControlableCloudEffect(uuid)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val locs = arrayListOf<RelativeLocation>()
                            val source = (
                                    PointsBuilder()
                                        .addDiscreteCircleXZ(scaledDistance(1.0), 480, 0.1)
                                    ).createWithoutClone()
                            val count = (waveData.getRandomCount()).coerceAtLeast(1)
                            if (source.isEmpty()) {
                                repeat(count) {
                                    locs.add(RelativeLocation(0.0, 0.0, 0.0))
                                }
                            } else {
                                val rand = Random.Default
                                repeat(count) {
                                    val base = source[rand.nextInt(source.size)]
                                    locs.add(RelativeLocation(base.x + 0.0, base.y + 0.0, base.z + 0.0))
                                }
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = waveData.getRandomSpeed()
                            wave.clone().apply {
                                maxAge = waveData.getRandomParticleMaxAge()
                                size = waveData.getRandomSize()
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
