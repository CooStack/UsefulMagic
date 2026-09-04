package cn.coostack.usefulmagic.entity.custom.dragon.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 实现粒子聚集到某个点的效果
 */
@CooAutoRegister
class TrackingTailEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    @CodecField
    var simpleConfig = SimpleRandomParticleData()

    @CodecField
    var particleConfig = ControlableCParticleData()

    @CodecField
    var offsetNoise = 0.5

    @CodecField
    var strength = 0.4

    @CodecField
    var arriveRadius = 0.5

    @CodecField
    var noiseStrength = 0.1

    @CodecField
    var trackingTarget = Vec3.ZERO

    @CodecField
    var velocity = Vec3.ZERO

    @CodecField
    var arrive = false

    @CodecField
    var arriveCanceled = true

    init {
        enableInterpolator = true
        emittersInterpolator.setRefiner(2.5)
    }


    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.ExpDrag(0.03,0.1),
            CParticleForce.Noise(noiseStrength, clampSpeed = 16.2)
        )
    }

    override fun doTick() {
        if (!arrive) {
            val nextV = PhysicsUtil.nextAttractVelocityNullable(
                pos,
                velocity,
                trackingTarget,
                strength = strength,
                arriveRadius = arriveRadius,
                maxSpeed = 12.0,
                falloffPow = 1
            )
            velocity = nextV ?: Vec3.ZERO
            if (nextV == null) {
                arrive = true
                if (arriveCanceled) {
                    canceled = true
                }
            }
        }
        pos += velocity
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = ArrayList<Pair<ControlableParticleData, RelativeLocation>>()

        repeat(simpleConfig.getRandomCount()) {
            res.add(
                particleConfig.clone().apply {
                    maxAge = simpleConfig.getRandomParticleMaxAge()
                    size = simpleConfig.getRandomSize()
                    speed = simpleConfig.getRandomSpeed()
                } to Vec3.ZERO.offsetRandomly(offsetNoise * Random.nextDouble()).asRelative()
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
}
