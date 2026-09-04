package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.phys.Vec3
import java.util.UUID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import net.minecraft.world.level.Level
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

@CooAutoRegister
class EndRodLineComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    @CodecField
    var end: RelativeLocation = RelativeLocation.zero()

    @CodecField
    var count: Int = 0
    var color: Vec3 = Vec3.ZERO

    @CodecField
    var maxAge: Int = 0

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val endDir = end.normalize()
        return PointsBuilder()
            .addLine(RelativeLocation().remove(endDir), end.clone().add(endDir), count)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
                }.addCParticleInstanceInit {
                    effect = ControlableEndRodEffect(UUID.randomUUID())
                    color = Math3DUtil.colorOf(
                        this@EndRodLineComposition.color.x.toInt(),
                        this@EndRodLineComposition.color.y.toInt(),
                        this@EndRodLineComposition.color.z.toInt()
                    )
                    this.maxAge = this@EndRodLineComposition.maxAge
                }
            }
    }

    @CodecField
    var age = 0
    override fun onDisplay() {
        addPreTickAction {
            if (age++ > maxAge) {
                remove()
            }
        }
    }

}

