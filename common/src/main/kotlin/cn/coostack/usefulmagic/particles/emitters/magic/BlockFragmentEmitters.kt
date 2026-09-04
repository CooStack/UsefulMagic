package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
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
class BlockFragmentEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.Noise(
                0.1, 1.3, 2.0, 15.0, 1.0, true
            )
        )
    }

    @CodecField
    var template = ControlableCParticleData().apply {
        velocity = Vec3.ZERO
        visibleRange = 256.0f
        color = Vector3f(0.996078f, 0.886275f, 0.164706f)
        alpha = 1.0f
        light = -1
        speedLimit = 32.0
        sign = 0
        this.blockCollision = true
    }

    @CodecField
    var randomData = SimpleRandomParticleData().apply {
        minAge = 10
        maxAge = 20
        minCount = 10
        maxCount = 20
        minSize = 0.1
        maxSize = 0.3
        minSpeed = 0.0
        maxSpeed = 0.0
    }

    @CodecField
    var range = 0.2

    init {
        maxTick = 1
        gravity = 0.025
    }

    override fun cparticleBlockCollisionRange(): Int {
        return super.cparticleBlockCollisionRange() * 2
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()
        // 发射器 1: Emitter 1
        run {
            res.addAll(
                PointsBuilder()
                    .addWith {
                        val rand = kotlin.random.Random.Default
                        val locs = arrayListOf<RelativeLocation>()
                        val count = randomData.getRandomCount()
                        repeat(count) {
                            val u = rand.nextDouble()
                            val v = rand.nextDouble()
                            val theta = 2.0 * Math.PI * u
                            val phi = acos(2.0 * v - 1.0)
                            val dx = sin(phi) * cos(theta)
                            val dy = cos(phi)
                            val dz = sin(phi) * sin(theta)
                            val rr = range * cbrt(rand.nextDouble())
                            locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                        }
                        locs
                    }
                    .createWithoutClone()
                    .map { rel ->
                        template.clone().apply {
                            maxAge = randomData.getRandomParticleMaxAge()
                            size = randomData.getRandomSize()
                        } to rel
                    }
            )

        }

        return res
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


    override fun doTick() {
        // modify emitter variables here
    }

}
