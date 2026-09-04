package cn.coostack.usefulmagic.particles.emitters.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCherryLeavesEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*

/**
 * 花朵在绽放
 *
 * @constructor
 * @param pos
 * @param world
 */
@CooAutoRegister
class BloomEffectEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var currentRadius = 0.2

    @CodecField
    var currentDiscrete = 0.4

    @CodecField
    var spreadSpeed = 0.5

    @CodecField
    var maxDiscrete = 5.0

    @CodecField
    var spreadDiscreteSpeed = 0.1

    @CodecField
    var maxRadius = 16.0

    @CodecField
    var yawSpeed = 0.0f minRangeTo 0.01f

    @CodecField
    var pitchSpeed = 0.0f minRangeTo 0.01f

    @CodecField
    var rollSpeed = 0.0f minRangeTo 0.01f

    @CodecField
    var templateData = ControlableCParticleData().apply {
        this.effect = ControlableCherryLeavesEffect(uuid)
    }

    @CodecField
    var simpleData = SimpleRandomParticleData()

    override fun doTick() {
        currentRadius += spreadSpeed
        if (currentDiscrete < maxDiscrete) {
            currentDiscrete += spreadDiscreteSpeed
            if (currentDiscrete > maxDiscrete) {
                currentDiscrete = maxDiscrete
            }
        }
        if (maxRadius <= currentRadius) {
            remove()
        }
    }

    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.Noise(0.04)
        )
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return PointsBuilder()
            .addDiscreteCircleXZ(currentRadius, simpleData.getRandomCount(), currentDiscrete)
            .createWithoutClone().map {
                templateData.clone().apply {
                    this.maxAge = simpleData.getRandomParticleMaxAge()
                    this.size = simpleData.getRandomSize()
                    if (!faceToCamera) {
                        this.yaw = (-PIF minRangeTo PIF).random()
                        this.pitch = (-PIF minRangeTo PIF).random()
                        this.roll = (-PIF minRangeTo PIF).random()

                        this.angularVelocity = Vector3f(
                            pitchSpeed.random(),
                            yawSpeed.random(),
                            rollSpeed.random()
                        )
                    }
                    this.alphaCurve = CParticleCurve.of(
                        0f to 0f,
                        0.5f to 1f,
                        1f to 0f
                    )
                } to it
            }
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