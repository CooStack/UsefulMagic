package cn.coostack.usefulmagic.particles.emitters.entity.eye

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.particles.particle.WaveParticleEffect
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

@CooAutoRegister
class EyeExclaimEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var exclaimCenter = ControlableCParticleData()
        .apply {
            effect = WaveParticleEffect(uuid)
            size = 0f
            alpha = 1f
            setTextureSheet(TextureSheetsEnum.PARTICLE_SHEET_TRANSLUCENT)
            alphaCurve = CParticleCurve.linear(1f, 0.1f)
            scaleCurve = CParticleCurve.linear(0f, 100f)
        }


    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return listOf(
            exclaimCenter to RelativeLocation().add(
                (Vec3.ZERO.random() * Random.nextDouble(0.4)).asRelative()
            ),
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
}