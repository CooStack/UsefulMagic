package cn.coostack.usefulmagic.particles.entity.dragon.composition.lifecycle.spawn

import cn.coostack.cooparticlesapi.animation.timeline.AngleAnimator
import cn.coostack.cooparticlesapi.animation.timeline.Eases
import cn.coostack.cooparticlesapi.animation.timeline.Timeline
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI

@CooAutoRegister
class MagicDragonSpawningFloorComposition(position: Vec3, world: Level? = null) :
    AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.964706F, 0.545098F, 0.992157F)

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)

    init {
        axis = RelativeLocation.yAxis()
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
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
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(32.0, 640)
                                    .addFourierSeries(
                                        FourierSeriesBuilder()
                                            .count(720)
                                            .scale(32 / 3.0)
                                            .addFourier(1.0, 2.0, 0.0)
                                            .addFourier(2.0, -1.0, 0.0)
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addFourierSeries(
                                                FourierSeriesBuilder()
                                                    .count(720)
                                                    .scale(32 / 3.0)
                                                    .addFourier(1.0, 2.0, 0.0)
                                                    .addFourier(2.0, -1.0, 0.0)
                                            )
                                            .rotateAsAxis(-0.333333 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addBuilder(
                                        RelativeLocation(20.75, 0.0, 11.625),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(-0.041667, 0.0, 0.125),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(0.041667, 0.0, -0.125),
                                                        PointsBuilder()
                                                            .addLine(
                                                                RelativeLocation(0.375, 0.0, 0.75),
                                                                RelativeLocation(-0.375, 0.0, 0.75),
                                                                60
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 1.5),
                                                                RelativeLocation(0.0, 0.0, -0.625),
                                                                60
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.375, 0.0, -0.25),
                                                                RelativeLocation(0.0, 0.0, -0.625),
                                                                60
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.375, 0.0, -0.25),
                                                                RelativeLocation(0.0, 0.0, -0.625),
                                                                60
                                                            )
                                                    )
                                                    .rotateAsAxis(0.333333 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                            )
                                            .scale(3.0)
                                    )
                                    .addBuilder(
                                        RelativeLocation(20.75, 0.0, -12.0),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(-20.75, 0.0, 12.0),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(20.75, 0.0, -15.0),
                                                        PointsBuilder()
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                RelativeLocation(0.75, 0.0, -0.25),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                RelativeLocation(-0.75, 0.0, -0.25),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 3.0),
                                                                RelativeLocation(0.625, 0.0, 2.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 3.0),
                                                                RelativeLocation(-0.625, 0.0, 2.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.625, 0.0, 2.0),
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.625, 0.0, 2.0),
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                30
                                                            )
                                                            .scale(3.0)
                                                    )
                                            )
                                            .rotateAsAxis(-0.333333 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, -24.0),
                                        PointsBuilder()
                                            .addLine(
                                                RelativeLocation(0.25, 0.0, 0.5625),
                                                RelativeLocation(-0.25, 0.0, 0.5625),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(0.625, 0.0, 0.0625),
                                                RelativeLocation(-0.625, 0.0, 0.0625),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(0.0, 0.0, 1.1875),
                                                RelativeLocation(0.0, 0.0, -1.0625),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(0.5, 0.0, -0.5625),
                                                RelativeLocation(-0.5, 0.0, -0.8125),
                                                30
                                            )
                                            .scale(3.0)
                                    )
                                    .addBuilder(
                                        RelativeLocation(-20.875, 0.0, -12.0),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(20.875, 0.0, 12.0),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(-20.935769, 0.0, -14.105298),
                                                        PointsBuilder()
                                                            .addCircle(RelativeLocation(0.0, 0.0, 0.916667), 0.5, 180)
                                                            .addLine(
                                                                RelativeLocation(0.011683, 0.0, 0.405666),
                                                                RelativeLocation(0.011683, 0.0, -0.636001),
                                                                60
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.386683, 0.0, -0.011001),
                                                                RelativeLocation(-0.363317, 0.0, -0.011001),
                                                                60
                                                            )
                                                            .addRadian(
                                                                RelativeLocation(0.004853, 0.0, 1.944586),
                                                                0.5,
                                                                60,
                                                                0.0,
                                                                1.0 * PI,
                                                                1.0 * PI
                                                            )
                                                            .scale(3.0)
                                                    )
                                            )
                                            .rotateAsAxis(-0.666667 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addBuilder(
                                        RelativeLocation(-20.75, 0.0, 11.875),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(20.75, 0.0, -11.875),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(-20.75, 0.0, 11.875),
                                                        PointsBuilder()
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, -1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, -1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, 1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, 1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, 1.0),
                                                                RelativeLocation(0.5, 0.0, -1.0),
                                                                30
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, 1.0),
                                                                RelativeLocation(-0.5, 0.0, -1.0),
                                                                30
                                                            )
                                                            .scale(2.5)
                                                    )
                                            )
                                            .rotateAsAxis(-0.333333 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 23.875),
                                        PointsBuilder()
                                            .addLine(
                                                RelativeLocation(0.0, 0.0, 1.0),
                                                RelativeLocation(0.0, 0.0, -1.0),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(0.0, 0.0, 1.0),
                                                RelativeLocation(-0.75, 0.0, 0.5),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(-0.75, 0.0, 0.5),
                                                RelativeLocation(0.0, 0.0, 0.0),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(0.0, 0.0, 0.0),
                                                RelativeLocation(-0.75, 0.0, -0.625),
                                                30
                                            )
                                            .addLine(
                                                RelativeLocation(-0.75, 0.0, -0.625),
                                                RelativeLocation(0.0, 0.0, -1.0),
                                                30
                                            )
                                            .scale(2.8)
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.4F
                                        color = this@MagicDragonSpawningFloorComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(1.738149, 1.218534, 0.0),
                                RelativeLocation(-8.408578, 0.159251, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 128)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        val angleOffsetCount2 = 6
        repeat(angleOffsetCount2) { index ->
            val finalAngle2 = (2.0 * PI) * (index / 6.0)
            result[
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                applyBuilder(
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.0, 0.0, 37.0), RelativeLocation(4.0, 0.0, 39.0), 30)
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 37.0),
                                            RelativeLocation(-4.0, 0.0, 39.0),
                                            30
                                        )
                                        .addLine(RelativeLocation(0.0, 0.0, 37.0), RelativeLocation(4.0, 0.0, 39.0), 30)
                                        .addLine(
                                            RelativeLocation(4.0, 0.0, 39.0),
                                            RelativeLocation(-4.0, 0.0, 39.0),
                                            30
                                        )
                                        .addBuilder(
                                            RelativeLocation(-0.041667, 0.0, 39.554487),
                                            PointsBuilder()
                                                .addBezierCurve(
                                                    RelativeLocation(0.5, 0.0, 1.0),
                                                    RelativeLocation(-0.75, 0.0, -0.75),
                                                    RelativeLocation(0.5, 0.0, -0.625),
                                                    RelativeLocation(-0.125, 0.0, 1.0),
                                                    40
                                                )
                                                .addBezierCurve(
                                                    RelativeLocation(-0.75, 0.0, -0.75),
                                                    RelativeLocation(0.375, 0.0, -0.5),
                                                    RelativeLocation(0.0, 0.0, -2.0),
                                                    RelativeLocation(1.25, 0.0, -0.875),
                                                    40
                                                )
                                                .scale(1.0)
                                        )
                                ) { shapeRel1 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                        }
                                        .addParticleInstanceInit {
                                            size = 0.4F
                                            color = this@MagicDragonSpawningFloorComposition.color
                                            textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(
                                        15,
                                        finalAngle2,
                                        Eases.bezierEase(
                                            RelativeLocation(0.401806, 1.035048, 0.0),
                                            RelativeLocation(-0.581264, 0.075461, 0.0)
                                        )
                                    )
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                    }
                                    addPreTickAction {
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
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(6, 160, 42.0)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(6, 160, 42.0)
                                            .rotateAsAxis(0.166667 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addCircle(43.0, 720)
                                    .addDottedCircle(45.0, 480, 12, 0.3)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.4F
                                        color = this@MagicDragonSpawningFloorComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(1.738149, 1.218534, 0.0),
                                RelativeLocation(-8.408578, 0.159251, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(0.015915 * PI)
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
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
        }
    }
}