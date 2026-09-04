package cn.coostack.usefulmagic.particles.emitters.explosion

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

@CooAutoRegister
class ExplosionAnimateLaserMagicEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    val random = Random(System.currentTimeMillis())

    @CodecField
    var templateData = ControlableCParticleData()

    @CodecField
    var minDiscrete = 1.0

    @CodecField
    var maxDiscrete = 10.0

    @CodecField
    var maxRadius = 10.0

    @CodecField
    var height = 100.0

    @CodecField
    var heightStep = 1.0

    @CodecField
    var radiusStep = 1.0

    @CodecField
    var minCount = 20

    @CodecField
    var maxCount = 120
    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return MathUtil.discreteCylinderGenerator(
            minDiscrete, maxDiscrete, maxRadius, height, heightStep, radiusStep, minCount, maxCount
        ).map {
            templateData.clone().apply {
                velocity = (it.normalize().offsetRandomly(0.5).normalize() * 0.1).toVector()
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
