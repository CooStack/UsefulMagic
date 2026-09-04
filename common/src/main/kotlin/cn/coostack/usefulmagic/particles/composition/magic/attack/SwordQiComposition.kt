package cn.coostack.usefulmagic.particles.composition.magic.attack

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

@CooAutoRegister
class SwordQiComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var movement: RelativeLocation = RelativeLocation(0, 0, 1)

    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        1.0,
        RelativeLocation(7.179487, 0.999128, 0.0),
        RelativeLocation(7.179487, 1.0, 0.0)
    )

    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addBuilder(
                    RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addLine(RelativeLocation(0.0, 3.0, 0.0), RelativeLocation(0.375, 2.5, 0.0), 30)
                        .addLine(RelativeLocation(0.0, 3.0, 0.0), RelativeLocation(-0.375, 2.5, 0.0), 30)
                        .addLine(RelativeLocation(0.375, 2.5, 0.0), RelativeLocation(0.375, 0.0, 0.0), 30)
                        .addLine(RelativeLocation(-0.375, 2.5, 0.0), RelativeLocation(-0.375, 0.0, 0.0), 30)
                        .addLine(RelativeLocation(0.0, 2.25, 0.0), RelativeLocation(0.0, 0.375, 0.0), 30)
                        .addLine(RelativeLocation(0.375, 0.0, 0.0), RelativeLocation(1.5, 0.0, 0.0), 30)
                        .addLine(RelativeLocation(-0.375, 0.0, 0.0), RelativeLocation(-1.5, 0.0, 0.0), 30)
                        .addLine(RelativeLocation(1.0, -0.5, 0.0), RelativeLocation(0.25, -0.5, 0.0), 10)
                        .addLine(RelativeLocation(-1.0, -0.5, 0.0), RelativeLocation(-0.25, -0.5, 0.0), 10)
                        .addLine(RelativeLocation(1.5, 0.0, 0.0), RelativeLocation(1.0, -0.5, 0.0), 10)
                        .addLine(RelativeLocation(-1.5, 0.0, 0.0), RelativeLocation(-1.0, -0.5, 0.0), 10)
                        .addLine(RelativeLocation(0.25, -0.5, 0.0), RelativeLocation(0.25, -1.5, 0.0), 10)
                        .addLine(RelativeLocation(-0.25, -0.5, 0.0), RelativeLocation(-0.25, -1.5, 0.0), 10)
                        .addLine(RelativeLocation(0.25, -1.5, 0.0), RelativeLocation(-0.25, -1.5, 0.0), 10)
                        .addFillTriangle(
                            RelativeLocation(0.375, 0.0, 0.0),
                            RelativeLocation(0.25, -0.5, 0.0),
                            RelativeLocation(1.125, -0.5, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(-0.375, 0.0, 0.0),
                            RelativeLocation(-0.25, -0.5, 0.0),
                            RelativeLocation(-1.125, -0.5, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(1.125, -0.5, 0.0),
                            RelativeLocation(0.375, 0.0, 0.0),
                            RelativeLocation(1.5, 0.0, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(-1.125, -0.5, 0.0),
                            RelativeLocation(-0.375, 0.0, 0.0),
                            RelativeLocation(-1.5, 0.0, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(0.375, 2.5, 0.0),
                            RelativeLocation(-0.375, 2.5, 0.0),
                            RelativeLocation(0.0, 3.0, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(0.25, 0.0, 0.0),
                            RelativeLocation(-0.25, 0.0, 0.0),
                            RelativeLocation(-0.25, -1.5, 0.0),
                            5.0
                        )
                        .addFillTriangle(
                            RelativeLocation(0.25, -1.5, 0.0),
                            RelativeLocation(-0.25, -1.5, 0.0),
                            RelativeLocation(0.25, 0.0, 0.0),
                            5.0
                        )
                )
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
        addPreTickAction {
            scaleHelper.doScale()
            rotateToPoint(movement)
        }
    }
}
