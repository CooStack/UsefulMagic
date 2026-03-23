package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.extend.relativize
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
class SwordLightComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    val scaleHelper = CompositionScaleHelper(0.01, 1.0, 20)

    @CodecField
    var lockedEntityID: Int = -1

    init {
        setDisabledInterval(20)
        scaleHelper.loadControler(this)
    }

    val option: Int
        get() = ParticleOption.getParticleCounts()

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(10.0, 180 * option, 3.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .rotateAsAxis(PI / 3)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }
                    .addParticleInstanceInit {
                        colorOfRGB(255, 255, 255)
                    }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus == 2) {
                scaleHelper.doScaleReversed()
            } else {
                scaleHelper.doScale()
            }
            rotateAsAxis(PI / 64.0)
            val entity = world!!.getEntity(lockedEntityID)
            if (entity != null && entity.isAlive) {
                val rel = this.position.relativize(entity.eyePosition.add(0.0, -0.2, 0.0))
                rotateToPoint(
                    RelativeLocation.of(
                        rel
                    )
                )
            }
        }
    }

}
