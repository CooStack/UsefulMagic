package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper

@CooAutoRegister
class BookShootSkillComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    val scaleHelper = CompositionScaleHelper(0.01, 1.0, 20)

    init {
        setDisabledInterval(20)
        scaleHelper.loadControler(this)
    }

    val option = ParticleOption.getParticleCounts()
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(5.0, 120 * option)
            .addPolygonInCircle(3, 10 * option, 5.0)
            .rotateAsAxis(PI / 3)
            .addPolygonInCircle(3, 10 * option, 5.0)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }.addParticleInstanceInit {
                    colorOfRGB(255, 200, 100)
                    size = 0.3f
                }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus != 2) {
                scaleHelper.doScale()
            } else {
                scaleHelper.doScaleReversed()
            }
            rotateAsAxis(PI / 64)
        }
    }

}
