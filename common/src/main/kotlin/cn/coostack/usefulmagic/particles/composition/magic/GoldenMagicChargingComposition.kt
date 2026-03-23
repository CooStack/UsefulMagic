package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.animation.timeline.*
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicClient
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

@CooAutoRegister
class GoldenMagicChargingComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    init {
        axis = RelativeLocation.yAxis()
        setDisabledInterval(20)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()
        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.727982, 1.0, 0.0),
                                RelativeLocation(4.093646, 1.086124, 0.0)
                            )
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(6.0, 120 * UsefulMagicClient.option)
                                    .addPolygonInCircle(4, 30 * UsefulMagicClient.option, 6.0)
                                    .rotateAsAxis(0.25 * PI)
                                    .addPolygonInCircle(4, 30 * UsefulMagicClient.option, 6.0)
                                    .addRadian(7.0, 80, -0.125 * PI, 0.125 * PI, 0.0)
                                    .addRadian(7.0, 80, -0.125 * PI, 0.125 * PI, 1.5 * PI)
                                    .addRadian(7.0, 80, -0.125 * PI, 0.125 * PI, 1.0 * PI)
                                    .addRadian(7.0, 80, -0.125 * PI, 0.125 * PI, 0.5 * PI)
                            ) { shapeRel0 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(RelativeLocation.yAxis(), PI / 64)
                                }
                                setReversedScaleOnCompositionStatus(this@GoldenMagicChargingComposition)
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        val angleOffsetCount = 5
        repeat(angleOffsetCount) { index ->
            val finalAngle = (2.0 * PI) * index.toDouble() / angleOffsetCount.toDouble()
            result[
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                loadScaleHelperBezierValue(
                                    0.01,
                                    1.0,
                                    18,
                                    RelativeLocation(10.14582, 1.073883, 0.0),
                                    RelativeLocation(10.64348, 1.152122, 0.0)
                                )
                                applyBuilder(
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.0, 0.0, 3.0), RelativeLocation(1.0, 0.0, 2.0), 30)
                                        .addLine(RelativeLocation(0.0, 0.0, 3.0), RelativeLocation(-1.0, 0.0, 2.0), 30)
                                ) { shapeRel0 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(20, finalAngle, Eases.inOutCubic)
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                        .step {
                                            if (!this@GoldenMagicChargingComposition.status.isDisable()) return@step false
                                            rotateAsAxis(animator.fadeDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                    }
                                    addPreTickAction {
                                        rotateAsAxis(-PI / 32)
                                    }
                                    setReversedScaleOnCompositionStatus(this@GoldenMagicChargingComposition)
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 0.0, 0.0)
        }

        return result
    }

    override fun isValid(): Boolean {
        return super.isValid() && this.status.isEnable()
    }

    override fun remove() {
        if (!status.isDisable()) {
            status.disable()
        } else {
            super.remove()
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            toggleRelative()
        }
    }
}