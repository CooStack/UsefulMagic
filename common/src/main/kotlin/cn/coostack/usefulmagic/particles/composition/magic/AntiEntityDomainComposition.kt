package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.animation.timeline.AngleAnimator
import cn.coostack.cooparticlesapi.animation.timeline.Eases
import cn.coostack.cooparticlesapi.animation.timeline.Timeline
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.*
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class AntiEntityDomainComposition(position: Vec3, world: Level? = null) :
    AutoSequencedParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.827451F, 0.313725F, 0.968627F)

    @CodecField
    var age: Int = 0

    val option: Int = 3

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)

    init {
        axis = RelativeLocation.yAxis()
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
        animate.addAnimate(1) { age > 0 }
            .addAnimate(1) { age > 10 }
            .addAnimate(1) { age > 20 }
        setDisabledInterval(10)
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val result: SortedMap<CompositionData, RelativeLocation> = TreeMap()
        var orderCounter = 0

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(48.0, 360 * option)
                                    .addCircle(44.0, 360 * option)
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(8, 24.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .addDottedLine(
                                                        RelativeLocation(3.0, 0.0, 34.0),
                                                        RelativeLocation(-6.0, 0.0, 0.0),
                                                        15 * option,
                                                        2,
                                                        0.5
                                                    )
                                                    .addDottedLine(
                                                        RelativeLocation(1.0, 0.0, 35.0),
                                                        RelativeLocation(-2.0, 0.0, 0.0),
                                                        10 * option,
                                                        2,
                                                        0.5
                                                    )
                                                p.rotateTo(-it)
                                                res.addAll(
                                                    p
                                                    .pointsOnEach { rel -> rel.add(it) }
                                                    .createWithoutClone()
                                                )
                                            }
                                        res
                                    }
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(10, 20.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .addDottedLine(
                                                        RelativeLocation(3.0, 0.0, 34.0),
                                                        RelativeLocation(-6.0, 0.0, 0.0),
                                                        15 * option,
                                                        2,
                                                        0.5
                                                    )
                                                    .addDottedLine(
                                                        RelativeLocation(1.0, 0.0, 35.0),
                                                        RelativeLocation(-2.0, 0.0, 0.0),
                                                        10 * option,
                                                        2,
                                                        0.5
                                                    )
                                                p.rotateTo(-it)
                                                res.addAll(
                                                    p
                                                    .pointsOnEach { rel -> rel.add(it) }
                                                    .createWithoutClone()
                                                )
                                            }
                                        res
                                    }
                                    .addDottedCircle(19.0, 180 * option, 12, 0.1)
                                    .addCircle(21.0, 240 * option)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(6, 30 * option, 19.0)
                                            .rotateAsAxis(-0.166667 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addPolygonInCircle(6, 30 * option, 19.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.3F
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        color = this@AntiEntityDomainComposition.color
                                    }
                            }
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(46.0, 120)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        size = 1.5F
                                        color = this@AntiEntityDomainComposition.color
                                        currentAge = Random.nextInt(lifetime)
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.713318, 1.355039, 0.0),
                                RelativeLocation(-6.331828, 0.358377, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 128)
                                }
                                setReversedScaleOnCompositionStatus(this@AntiEntityDomainComposition)
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(8.0, 80 * option)
                                    .addPolygonInCircle(5, 20 * option, 8.0)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(5, 20 * option, 8.0)
                                            .rotateAsAxis(0.2 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(6, 0.2)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .axis(RelativeLocation(0.0, 0.0, -1.0))
                                                    .addLine(
                                                        RelativeLocation(0.0, 0.0, 4.0),
                                                        RelativeLocation(1.0, 0.0, 3.0),
                                                        30
                                                    )
                                                    .addLine(
                                                        RelativeLocation(0.0, 0.0, 4.0),
                                                        RelativeLocation(-1.0, 0.0, 3.0),
                                                        30
                                                    )
                                                p.rotateTo(-it)
                                                res.addAll(
                                                    p
                                                    .pointsOnEach { rel -> rel.add(it) }
                                                    .createWithoutClone()
                                                )
                                            }
                                        res
                                    }
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        color = this@AntiEntityDomainComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.713318, 1.355039, 0.0),
                                RelativeLocation(-6.331828, 0.358377, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(-PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            repeat(6) { index ->
                                val finalAngle = (-2.0 * PI) * (index / 6.0)
                                applyPoint(RelativeLocation(0.0, 0.0, 0.0)) { shapeRel1 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation.yAxis()
                                                    applyPoint(RelativeLocation(0.0, 0.0, 32.0)) { shapeRel2 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withComposition(
                                                                    ParticleShapeComposition(it).apply {
                                                                        axis = RelativeLocation.yAxis()
                                                                        applyBuilder(
                                                                            PointsBuilder()
                                                                                .addCircle(8.0, 120 * option)
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        0.689048,
                                                                                        0.0,
                                                                                        6.771566
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -0.810952,
                                                                                        0.0,
                                                                                        6.521566
                                                                                    ),
                                                                                    RelativeLocation(-0.125, 0.0, -2.0),
                                                                                    RelativeLocation(0.625, 0.0, 1.125),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -0.560952,
                                                                                        0.0,
                                                                                        5.771566
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        0.439048,
                                                                                        0.0,
                                                                                        7.271566
                                                                                    ),
                                                                                    RelativeLocation(-0.25, 0.0, 0.75),
                                                                                    RelativeLocation(
                                                                                        0.246022,
                                                                                        0.0,
                                                                                        -0.559123
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        4.960941,
                                                                                        0.0,
                                                                                        5.328671
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        4.960941,
                                                                                        0.0,
                                                                                        4.078671
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        0.875,
                                                                                        0.0,
                                                                                        -1.375
                                                                                    ),
                                                                                    RelativeLocation(-2.5, 0.0, 0.25),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        4.960941,
                                                                                        0.0,
                                                                                        5.328671
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        4.024928,
                                                                                        0.0,
                                                                                        4.199855
                                                                                    ),
                                                                                    RelativeLocation(-0.875, 0.0, 0.0),
                                                                                    RelativeLocation(
                                                                                        1.436013,
                                                                                        0.0,
                                                                                        0.003816
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        6.123834,
                                                                                        0.0,
                                                                                        0.220237
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        6.623834,
                                                                                        0.0,
                                                                                        -0.154763
                                                                                    ),
                                                                                    RelativeLocation(0.375, 0.0, 0.5),
                                                                                    RelativeLocation(-0.875, 0.0, 0.0),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        7.092268,
                                                                                        0.0,
                                                                                        0.409
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        6.467268,
                                                                                        0.0,
                                                                                        -0.966
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -0.75,
                                                                                        0.0,
                                                                                        -0.875
                                                                                    ),
                                                                                    RelativeLocation(0.625, 0.0, 1.375),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        5.094825,
                                                                                        0.0,
                                                                                        -4.724062
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        4.469825,
                                                                                        0.0,
                                                                                        -4.349062
                                                                                    ),
                                                                                    RelativeLocation(-1.0, 0.0, -0.125),
                                                                                    RelativeLocation(
                                                                                        0.242641,
                                                                                        0.0,
                                                                                        -0.117641
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -0.061441,
                                                                                        0.0,
                                                                                        -7.0625
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -0.061441,
                                                                                        0.0,
                                                                                        -5.9375
                                                                                    ),
                                                                                    RelativeLocation(-1.5, 0.0, 1.375),
                                                                                    RelativeLocation(1.75, 0.0, -1.5),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -0.061441,
                                                                                        0.0,
                                                                                        -7.0625
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -0.061441,
                                                                                        0.0,
                                                                                        -5.9375
                                                                                    ),
                                                                                    RelativeLocation(-0.5, 0.0, 0.625),
                                                                                    RelativeLocation(0.75, 0.0, -0.5),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -4.346814,
                                                                                        0.0,
                                                                                        -4.346814
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -4.346814,
                                                                                        0.0,
                                                                                        -4.346814
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -1.007359,
                                                                                        0.0,
                                                                                        0.117641
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -0.007359,
                                                                                        0.0,
                                                                                        -1.132359
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -6.474576,
                                                                                        0.0,
                                                                                        0.055085
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -5.849576,
                                                                                        0.0,
                                                                                        0.805085
                                                                                    ),
                                                                                    RelativeLocation(1.0, 0.0, -0.875),
                                                                                    RelativeLocation(
                                                                                        -2.375,
                                                                                        0.0,
                                                                                        -0.875
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addBezierCurve(
                                                                                    RelativeLocation(
                                                                                        -4.506963,
                                                                                        0.0,
                                                                                        5.156328
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -4.374604,
                                                                                        0.0,
                                                                                        4.523968
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        1.375,
                                                                                        0.0,
                                                                                        -0.375
                                                                                    ),
                                                                                    RelativeLocation(
                                                                                        -2.007359,
                                                                                        0.0,
                                                                                        -0.617641
                                                                                    ),
                                                                                    20 * option
                                                                                )
                                                                                .addCircle(5.0, 80 * option)
                                                                                .addFourierSeries(
                                                                                    FourierSeriesBuilder()
                                                                                        .count(360)
                                                                                        .scale(1.0)
                                                                                        .addFourier(3.0, -2.0, 30.0)
                                                                                        .addFourier(2.0, 3.0, 0.0)
                                                                                )
                                                                        ) { shapeRel3 ->
                                                                            CompositionData()
                                                                                .setDisplayerSupplier {
                                                                                    ParticleDisplayer.withSingle(
                                                                                        ControlableEndRodEffect(it)
                                                                                    )
                                                                                }
                                                                                .addParticleInstanceInit {
                                                                                    textureSheet =
                                                                                        CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                                                                    color =
                                                                                        this@AntiEntityDomainComposition.color
                                                                                    size = 0.35F
                                                                                }
                                                                        }
                                                                        loadScaleHelperBezierValue(
                                                                            0.01,
                                                                            1.0,
                                                                            10,
                                                                            RelativeLocation(4.221219, 1.281067, 0.0),
                                                                            RelativeLocation(-5.846501, 0.307412, 0.0)
                                                                        )
                                                                    }
                                                                )
                                                            }
                                                    }
                                                    applyDisplayAction {
                                                        val animator = AngleAnimator(20, finalAngle, Eases.outCubic)
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
                                                            rotateAsAxis(-PI / 72)
                                                        }
                                                    }
                                                }
                                            )
                                        }
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
            age++
        }
    }
}