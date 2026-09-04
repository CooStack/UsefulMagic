package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import java.util.UUID

@CooAutoRegister
class SwordComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    private val scaleHelper = CompositionScaleHelper(0.01, 0.5, 5)

    @CodecField
    var direction = RelativeLocation.zAxis()

    init {
        scaleHelper.loadControler(this)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addLine(RelativeLocation(0.0, 0.0, 4.0), RelativeLocation(1.0, 0.0, 3.0), 15)
                .addLine(RelativeLocation(0.0, 0.0, 4.0), RelativeLocation(-1.0, 0.0, 3.0), 15)
                .addLine(RelativeLocation(1.0, 0.0, 3.0), RelativeLocation(1.0, 0.0, -1.0), 30)
                .addLine(RelativeLocation(-1.0, 0.0, 3.0), RelativeLocation(-1.0, 0.0, -1.0), 30)
                .addBezierCurve(
                    RelativeLocation(0.0, 0.0, 3.0),
                    RelativeLocation(0.0, 0.0, 2.0),
                    RelativeLocation(1.0, 0.0, -0.4),
                    RelativeLocation(0.5, 0.0, -3.5),
                    70
                )
                .addBezierCurve(
                    RelativeLocation(0.0, 0.0, 2.0),
                    RelativeLocation(0.0, 0.0, 0.0),
                    RelativeLocation(-1.0, 0.0, -1.266667),
                    RelativeLocation(-0.875, 0.0, -2.625),
                    70
                )
                .addLine(RelativeLocation(1.0, 0.0, -1.0), RelativeLocation(2.5, 0.0, -1.0), 15)
                .addLine(RelativeLocation(-1.0, 0.0, -1.0), RelativeLocation(-2.5, 0.0, -1.0), 15)
                .addLine(RelativeLocation(2.5, 0.0, -1.0), RelativeLocation(2.0, 0.0, -2.0), 15)
                .addLine(RelativeLocation(-2.5, 0.0, -1.0), RelativeLocation(-2.0, 0.0, -2.0), 15)
                .addLine(RelativeLocation(2.0, 0.0, -2.0), RelativeLocation(0.5, 0.0, -2.0), 15)
                .addLine(RelativeLocation(-2.0, 0.0, -2.0), RelativeLocation(-0.5, 0.0, -2.0), 15)
                .addLine(RelativeLocation(0.5, 0.0, -2.0), RelativeLocation(0.5, 0.0, -3.25), 15)
                .addLine(RelativeLocation(-0.5, 0.0, -2.0), RelativeLocation(-0.5, 0.0, -3.25), 15)
                .addLine(RelativeLocation(0.5, 0.0, -3.25), RelativeLocation(0.0, 0.0, -3.25), 10)
                .addLine(RelativeLocation(-0.5, 0.0, -3.25), RelativeLocation(0.0, 0.0, -3.25), 10)
                .axis(RelativeLocation.zAxis())
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                        }
                        .addCParticleInstanceInit {
                            effect = ControlableEndRodEffect(UUID.randomUUID())
                        }
                }
        )

        return result
    }

    override fun onDisplay() {
        axis = RelativeLocation.zAxis()
        addPreTickAction {
            scaleHelper.doScale()
            rotateToPoint(direction)
        }
    }
}
