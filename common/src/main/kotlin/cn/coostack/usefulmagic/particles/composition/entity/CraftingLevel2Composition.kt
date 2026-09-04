package cn.coostack.usefulmagic.particles.composition.entity

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * level 2
 */
@CooAutoRegister
class CraftingLevel2Composition(
    position: Vec3, world: Level? = null
) : AutoParticleComposition(position, world) {

    val options: Int
        get() = ParticleOption.getParticleCounts()
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(2.0, 60, 0.5)
            .addPolygonInCircle(4, 30, 7.0)
            .addPolygonInCircle(4, 30, 6.0)
            .rotateAsAxis(PI / 4)
            .addPolygonInCircle(4, 30, 7.0)
            .addPolygonInCircle(4, 30, 6.0)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                }.addCParticleInstanceInit {
                    effect = ControlableEndRodEffect(UUID.randomUUID())
                    color = Math3DUtil.colorOf(240, 100, 240)
                }
            }
    }

    init {
        status.closedInternal = 20
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus != 2) {
                playCParticleAlphaTransition(20f, CParticleCurve.linear(0f, 1f))
            } else {
                playCParticleAlphaTransition(20f, CParticleCurve.linear(1f, 0f))
            }
            rotateAsAxis(PI / 64)
        }
    }
}
