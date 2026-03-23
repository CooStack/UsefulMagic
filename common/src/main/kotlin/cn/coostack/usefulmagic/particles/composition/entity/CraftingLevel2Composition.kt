package cn.coostack.usefulmagic.particles.composition.entity

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
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
    val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 20)
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
                    ParticleDisplayer.Companion.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }.addParticleInstanceInit {
                    colorOfRGB(240, 100, 240)
                }
            }
    }

    init {
        status.closedInternal = 20
        alphaHelper.loadControler(this)
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus != 2) {
                alphaHelper.increaseAlpha()
            } else {
                alphaHelper.decreaseAlpha()
            }
            rotateAsAxis(PI / 64)
        }
    }
}