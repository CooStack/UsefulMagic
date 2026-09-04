package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleColorCurve
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

@CooAutoRegister
class StarryMeteoriteLocusEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    @CodecField
    var templateData = ControlableCParticleData()

    init {
        enableInterpolator = true
        emittersInterpolator.setRefiner(4.0)
    }

    override fun doTick() {
    }

    override fun cparticleForces(): List<CParticleForce> = listOf(
        CParticleForce.Noise(0.05)
    )

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return listOf(templateData.apply {
            colorCurve =
                CParticleColorCurve.linear(
                    Math3DUtil.colorOf(255, 104, 138),
                    Vector3f(100 / 255f, 0f, 1f)
                )
        } to RelativeLocation())
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
