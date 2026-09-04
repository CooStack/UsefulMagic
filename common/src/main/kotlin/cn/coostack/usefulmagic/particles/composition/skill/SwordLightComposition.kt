package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.PI

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
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                }
                    .addCParticleInstanceInit {
                        effect = ControlableEndRodEffect(UUID.randomUUID())
                        color = Math3DUtil.colorOf(255, 255, 255)
                    }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.isDisable()) {
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
