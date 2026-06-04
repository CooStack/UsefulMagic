package cn.coostack.usefulmagic.entity.custom.dragon.spawn.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

@CooAutoRegister
class CollectEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    @CodecField
    var particleTemplate = ControlableParticleData()

    @CodecField
    var randomData = SimpleRandomParticleData()

    @CodecField
    var targetCollect = Vec3.ZERO

    @CodecField
    var defaultParticleVelocity = Vec3.ZERO

    @CodecField
    var arriveRange = 0.5

    @CodecField
    var strength = 1.2

    @CodecField
    var generateOffsetRange = 0.1 minRangeTo 0.5

    @CodecField
    var discardWhenArrive = false

    override fun doTick() {
    }


    /**
     * 初始自带切向初速度
     *
     * @param lerpProgress
     * @return
     */
    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        repeat(randomData.getRandomCount()) {
            res.add(
                particleTemplate.apply {
                    maxAge = randomData.getRandomParticleMaxAge()
                    size = randomData.getRandomSize()
                    velocity = defaultParticleVelocity * randomData.getRandomSpeed()
                } to RelativeLocation().offsetRandomly(generateOffsetRange.random())
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
        controler.addPreTickAction {
            val next = PhysicsUtil.nextAttractVelocityNullable(
                pos,
                data.velocity,
                targetCollect,
                strength = strength,
                falloffPow = 1,
                maxSpeed = 8.0,
                arriveRadius = arriveRange
            )
            data.velocity = next ?: Vec3.ZERO
            if (discardWhenArrive && next == null) {
                this.remove()
            }
        }
    }
}