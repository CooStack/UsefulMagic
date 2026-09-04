package cn.coostack.usefulmagic.particles.composition.magic.attack

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.animation.timeline.*
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random
import org.joml.Vector3f
import java.util.UUID

@CooAutoRegister
class MeteoriteChargingComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.996078F, 0.462745F, 0.462745F)

    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        4.0,
        RelativeLocation(0.17106, 0.49026, 0.0),
        RelativeLocation(-0.771523, -0.116883, 0.0)
    )
    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
        setDisabledInterval(20)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        val angleOffsetCount1 = 3
        repeat(angleOffsetCount1) { index ->
            val finalAngle1 = (2.0 * PI) * index.toDouble() / angleOffsetCount1.toDouble()
            result[
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                applyPoint(RelativeLocation(0.0, 0.0, 8.0)) { shapeRel0 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation.yAxis()
                                                    loadScaleHelperBezierValue(
                                                        0.01,
                                                        1.0,
                                                        10,
                                                        RelativeLocation(5.369008, 0.999905, 0.0),
                                                        RelativeLocation(5.386845, 0.996246, 0.0)
                                                    )
                                                    applyBuilder(
                                                        PointsBuilder()
                                                            .addBuilder(
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                PointsBuilder()
                                                                    .addCircle(2.0, 120)
                                                            )
                                                            .addBuilder(
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                PointsBuilder()
                                                                    .addFourierSeries(
                                                                        FourierSeriesBuilder()
                                                                            .count(360)
                                                                            .scale(1.0)
                                                                            .addFourier(2.0, 3.0, 0.0)
                                                                            .addFourier(3.0, -2.0, 0.0)
                                                                    )
                                                                    .scale(0.4)
                                                            )
                                                    ) { shapeRel1 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withCParticle(it)
                                                            }
                                                            .addCParticleInstanceInit {
                                                                effect = ControlableEndRodEffect(UUID.randomUUID())
                                                                color = this@MeteoriteChargingComposition.color
                                                            }
                                                    }
                                                    applyDisplayAction {
                                                        addPreTickAction {
                                                            rotateAsAxis(0.047746 * PI)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(20, finalAngle1, Eases.outCubic)
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                        rotateAsAxis(0.015915 * PI)
                                    }
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 0.0, 0.0)
        }

        val angleOffsetCount2 = 3
        repeat(angleOffsetCount2) { index ->
            val finalAngle2 = (2.0 * PI) * index.toDouble() / angleOffsetCount2.toDouble()
            result[
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                applyPoint(RelativeLocation(0.0, 0.0, -8.0)) { shapeRel0 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation.yAxis()
                                                    loadScaleHelperBezierValue(
                                                        0.01,
                                                        1.0,
                                                        10,
                                                        RelativeLocation(5.369008, 0.999905, 0.0),
                                                        RelativeLocation(5.386845, 0.996246, 0.0)
                                                    )
                                                    applyBuilder(
                                                        PointsBuilder()
                                                            .addBuilder(
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                PointsBuilder()
                                                                    .addPolygonInCircle(4, 30, 1.0)
                                                                    .addPolygonInCircle(4, 30, 1.0)
                                                                    .addPolygonInCircle(4, 30, 1.0)
                                                                    .rotateAsAxis(
                                                                        -0.25 * PI,
                                                                        RelativeLocation(0.0, 1.0, 0.0)
                                                                    )
                                                                    .addPolygonInCircle(4, 30, 1.0)
                                                            )
                                                            .addPolygonInCircle(4, 30, 2.0)
                                                            .addLine(
                                                                RelativeLocation(-1.0, 0.0, 2.0),
                                                                RelativeLocation(0.0, 0.0, 3.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 3.0),
                                                                RelativeLocation(1.0, 0.0, 2.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(2.0, 0.0, 1.0),
                                                                RelativeLocation(3.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-2.0, 0.0, 1.0),
                                                                RelativeLocation(-3.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(3.0, 0.0, 0.0),
                                                                RelativeLocation(2.0, 0.0, -1.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-3.0, 0.0, 0.0),
                                                                RelativeLocation(-2.0, 0.0, -1.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 4.0),
                                                                RelativeLocation(-0.5, 0.0, 3.5),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 4.0),
                                                                RelativeLocation(0.5, 0.0, 3.5),
                                                                30
                                                            )
                                                            .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                            .addBuilder(
                                                                RelativeLocation(0.043301, 0.0, -1.549358),
                                                                PointsBuilder()
                                                                    .addRadian(2.0, 80, -0.833333 * PI, -0.166667 * PI)
                                                            )
                                                            .rotateTo(RelativeLocation(0.0, -1.0, 0.0))
                                                            .scale(0.75)
                                                    ) { shapeRel1 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withCParticle(it)
                                                            }
                                                            .addCParticleInstanceInit {
                                                                effect = ControlableEndRodEffect(UUID.randomUUID())
                                                                color = this@MeteoriteChargingComposition.color
                                                            }
                                                    }
                                                    applyDisplayAction {
                                                        addPreTickAction {
                                                            rotateToPoint(shapeRel0.clone())
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(20, finalAngle2, Eases.outCubic)
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                        rotateAsAxis(0.015915 * PI)
                                    }
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 0.0, 0.0)
        }

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
                                RelativeLocation(5.877369, 1.22036, 0.0),
                                RelativeLocation(5.859532, 1.236565, 0.0)
                            )
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(3, 100, 8.0)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(3, 100, 8.0)
                                            .rotateAsAxis(-0.334698 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addCircle(10.0, 720)
                                    .addCircle(12.0, 720)
                            ) { shapeRel0 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        color = this@MeteoriteChargingComposition.color
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(0.015915 * PI)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

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
                                RelativeLocation(5.877369, 1.22036, 0.0),
                                RelativeLocation(5.877369, 1.22036, 0.0)
                            )
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(11.0, 30)
                            ) { shapeRel0 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                        age = Random.nextInt(maxAge)
                                        size = 0.8F
                                        color = this@MeteoriteChargingComposition.color
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(RelativeLocation.yAxis(), -0.015915 * PI)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        return result
    }

    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (!status.isDisable()) {
                scaleHelper.doScale()
            } else {
                scaleHelper.doScaleReversed()
                playCParticleAlphaTransition(10f, CParticleCurve.linear(1f, 0.1f))
            }
            toggleRelative()
        }
    }
}
