package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import java.util.UUID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

@CooAutoRegister
class GiantSwordComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {

    var direction = RelativeLocation()
    val option: Int
        get() = ParticleOption.getParticleCounts()

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addLine(
                Vec3(-2.0, 0.0, 0.0),
                Vec3(2.0, 0.0, 0.0),
                20 * option,
            )
            .addLine(
                Vec3(0.0, -2.0, 0.0),
                Vec3(0.0, 8.0, 0.0),
                40 * option,
            )
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                }
                    .addCParticleInstanceInit {
                        effect = ControlableEndRodEffect(UUID.randomUUID())
                        color = Math3DUtil.colorOf(255, 255, 255)
                    }
            }
    }

    override fun onDisplay() {
    }

}

