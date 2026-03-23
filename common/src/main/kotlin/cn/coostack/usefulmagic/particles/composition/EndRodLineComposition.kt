package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.client.particle.ParticleRenderType
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
                    ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                }.addParticleInstanceInit {
                    colorOfRGB(
                        this@EndRodLineComposition.color.x.toInt(),
                        this@EndRodLineComposition.color.y.toInt(),
                        this@EndRodLineComposition.color.z.toInt()
                    )
                    this.lifetime = this@EndRodLineComposition.maxAge
                    textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                }.addParticleControlerInstanceInit {
                    addPreTickAction {
                        this.currentAge = age
                    }
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

