package cn.coostack.usefulmagic.particles.composition.entity

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

@CooAutoRegister
class CraftingLevel1Style(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var r: Double = 1.0

    @CodecField
    var count: Int = 160

    val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 20)

    init {
        alphaHelper.loadControler(this)
        status.closedInternal = 20
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(r, count)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }.addParticleInstanceInit {
                    colorOfRGB(240, 100, 240)
                }
            }
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