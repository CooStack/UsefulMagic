package cn.coostack.usefulmagic.particles.composition.skill

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import java.util.UUID
import kotlin.math.PI
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

@CooAutoRegister
class TaiChiComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    init {
        setDisabledInterval(30)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val res = mutableMapOf<CompositionData, RelativeLocation>()
        res[CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(it)
                    .applyBuilder(
                        PointsBuilder()
                            .addCircle(0.5, 120)
                            .pointsOnEach {
                                it.x += 1.5
                            }
                    ) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                    }.applyBuilder(
                        PointsBuilder()
                            .addCircle(0.5, 120)
                            .pointsOnEach {
                                it.x -= 1.5
                            }
                    ) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }.addParticleInstanceInit {
                            colorOfRGB(0, 0, 0)
                        }
                    }.applyBuilder(
                        PointsBuilder()
                            .addHalfCircle(1.5, 120)
                            .pointsOnEach { p -> p.x += 3.0 }
                            .addHalfCircle(1.5, 120, PI)
                            .pointsOnEach { p -> p.x -= 1.5 }
                    ) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                    }.applyBuilder(
                        PointsBuilder()
                            .addCircle(3.0, 240)
                    ) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .applyDisplayAction {
                        this.setReversedScaleOnCompositionStatus(this@TaiChiComposition)
                        this.addPreTickAction {
                            rotateAsAxis(PI / 64)
                        }
                    }
            )
        }] = RelativeLocation(0.0, 0.01, 0.0)
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            rotateAsAxis(PI / 32)
        }
    }

}

