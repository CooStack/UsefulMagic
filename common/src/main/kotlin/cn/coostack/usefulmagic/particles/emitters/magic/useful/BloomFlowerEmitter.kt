package cn.coostack.usefulmagic.particles.emitters.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableMyceliumEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

@CooAutoRegister
class BloomFlowerEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.Noise(0.03, 0.15, 0.12, 0.8, 1.0, true),
            CParticleForce.FlowField(0.05, 0.5, 0.1, 1.0),
            CParticleForce.ExpDrag(0.15, 0.0, 0.0),
            CParticleForce.Vortex({ this.pos }, Vec3(0.0, 1.0, 0.0), 0.05, 0.03, 0.0, 36.0, 5.0, 0.2)
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
    }

    @CodecField
    var template = ControlableCParticleData().apply {
        velocity = Vec3(0.0, 0.0, 0.0)
        visibleRange = 128.0f
        color = Vector3f(1.0f, 1.0f, 1.0f)
        alpha = 1.0f
        light = 15
        faceToCamera = true
        speedLimit = 32.0
        effect = ControlableMyceliumEffect(uuid)
    }

    @CodecField
    var randomData = SimpleRandomParticleData().apply {
        minAge = 60
        maxAge = 70
        minCount = 8
        maxCount = 10
        minSize = 0.1
        maxSize = 0.3
        minSpeed = 1.0
        maxSpeed = 1.2
    }

    @CodecField
    var boxSize = 32.0

    init {
        delay = 1
        maxTick = -1
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val rand = Random.Default
                            val locs = arrayListOf<RelativeLocation>()
                            val count = randomData.getRandomCount()
                            val density = 0.0
                            val surface = false
                            val pw = 1.0 + 3.0 * density
                            repeat(count) {
                                val u0x = rand.nextDouble() - 0.5
                                var x = (if (u0x < 0.0) -1.0 else 1.0) * abs(u0x).pow(pw) * boxSize
                                val u0y = rand.nextDouble() - 0.5
                                var y = (if (u0y < 0.0) -1.0 else 1.0) * abs(u0y).pow(pw) * 10.0
                                val u0z = rand.nextDouble() - 0.5
                                var z = (if (u0z < 0.0) -1.0 else 1.0) * abs(u0z).pow(pw) * boxSize
                                if (surface) {
                                    when (rand.nextInt(3)) {
                                        0 -> x = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * boxSize
                                        1 -> y = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * 10.0
                                        2 -> z = (if (rand.nextDouble() < 0.5) -0.5 else 0.5) * boxSize
                                    }
                                }
                                locs.add(RelativeLocation(x + 0.0, y + 0.0, z + 0.0))
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = randomData.getRandomSpeed()
                            template.clone().apply {
                                maxAge = randomData.getRandomParticleMaxAge()
                                size = randomData.getRandomSize()
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