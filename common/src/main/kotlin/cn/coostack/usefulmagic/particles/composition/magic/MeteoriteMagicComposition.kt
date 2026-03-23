package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.animation.timeline.*
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random
import java.util.SortedMap
import java.util.TreeMap
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper

@CooAutoRegister
class MeteoriteMagicComposition(position: Vec3, world: Level? = null) :
    AutoSequencedParticleComposition(position, world) {
    @CodecField
    var direction: RelativeLocation = RelativeLocation(0.0, -1.0, 0.0)

    @CodecField
    var color: Vector3f = Vector3f(0.858824F, 0.341176F, 0.341176F)

    @CodecField
    var age: Int = 0


    val option: Int = 3

    private val scaleHelper = CompositionBezierScaleHelper(
        20,
        0.01,
        1.0,
        RelativeLocation(9.308036, 1.141461, 0.0),
        RelativeLocation(9.397321, 1.169344, 0.0)
    )


    private val alphaHelper = CompositionAlphaHelper(0.1, 1.0, 20)

    init {
        axis = RelativeLocation(0.0, -1.0, 0.0)
        scaleHelper.loadControler(this)
        setDisabledInterval(20)
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
        animate.addAnimate(2) { age > 0 }
            .addAnimate(5) { age > 60 }
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
                            repeat(4) { index ->
                                val finalAngle = (2.0 * PI) * (index / 4.0)
                                applyPoint(RelativeLocation(0.0, 0.0, 0.0)) { shapeRel1 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation.yAxis()
                                                    applyBuilder(
                                                        PointsBuilder()
                                                            .addFillTriangle(
                                                                RelativeLocation(1.0, 0.0, 4.0),
                                                                RelativeLocation(-1.0, 0.0, 4.0),
                                                                RelativeLocation(0.0, 0.0, 3.625),
                                                                8.0
                                                            )
                                                            .scale(5.0)
                                                    ) { shapeRel2 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                                            }
                                                            .addParticleInstanceInit {
                                                                size = 0.5F
                                                                color = this@MeteoriteMagicComposition.color
                                                                textureSheet =
                                                                    CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                                            }

                                                    }
                                                    loadScaleHelperBezierValue(
                                                        0.01,
                                                        1.0,
                                                        10,
                                                        RelativeLocation(6.026786, 1.168749, 0.0),
                                                        RelativeLocation(-4.017857, 0.17374, 0.0)
                                                    )
                                                    applyDisplayAction {
                                                        val animator = AngleAnimator(
                                                            20,
                                                            finalAngle,
                                                            Eases.bezierEase(
                                                                RelativeLocation(0.324777, 0.947871, 0.0),
                                                                RelativeLocation(-0.672991, -0.031158, 0.0)
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
                                                            rotateToWithAngle(direction, PI / 64)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                }
                            }
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(4, 100, 18.0)
                                    .addDiscreteCircleXZ(18.0, 360, 0.4)
                                    .rotateAsAxis(0.25 * PI)
                                    .addPolygonInCircle(4, 100, 18.0)
                                    .addFillTriangle(
                                        RelativeLocation(6.333333, 0.0, 2.333333),
                                        RelativeLocation(5.0, 0.0, 5.0),
                                        RelativeLocation(2.333333, 0.0, 6.333333),
                                        3.0
                                    )
                                    .addFillTriangle(
                                        RelativeLocation(-6.333333, 0.0, 2.333333),
                                        RelativeLocation(-5.0, 0.0, 5.0),
                                        RelativeLocation(-2.333333, 0.0, 6.333333),
                                        3.0
                                    )
                                    .addFillTriangle(
                                        RelativeLocation(6.333333, 0.0, -2.333333),
                                        RelativeLocation(5.0, 0.0, -5.0),
                                        RelativeLocation(2.333333, 0.0, -6.333333),
                                        3.0
                                    )
                                    .addFillTriangle(
                                        RelativeLocation(-6.333333, 0.0, -2.333333),
                                        RelativeLocation(-5.0, 0.0, -5.0),
                                        RelativeLocation(-2.333333, 0.0, -6.333333),
                                        3.0
                                    )
                                    .scale(2.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        color = this@MeteoriteMagicComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        size = 0.5F
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(2.354911, 1.255872, 0.0),
                                RelativeLocation(-7.611607, 0.281553, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 20.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(64.0, 120 * option, 14.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        currentAge = Random.nextInt(lifetime)
                                        size = 2.0F
                                        color = this@MeteoriteMagicComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(1.975446, 1.225138, 0.0),
                                RelativeLocation(-8.002232, 0.258671, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, -PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 20.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addFourierSeries(
                                        FourierSeriesBuilder()
                                            .count(240 * option)
                                            .scale(20.0 / 6)
                                            .addFourier(1.0, 4.0, 0.0)
                                            .addFourier(5.0, -5.0, 0.0)
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.5F
                                        color = this@MeteoriteMagicComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                                    .addParticleControlerInstanceInit {
                                        var time: Float = 0.0F
                                        addPreTickAction {
                                            if (this@MeteoriteMagicComposition.status.isEnable()) {
                                                time++
                                                particle.particleAlpha = (time / 20).toFloat()
                                            }
                                        }
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 128)
                                }
                            }

                        }
                    )
                }
        ] = RelativeLocation(0.0, -10.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(4, 150, 18.0)
                                    .rotateAsAxis(0.25 * PI)
                                    .addPolygonInCircle(4, 150, 18.0)
                                    .scale(3.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        color = this@MeteoriteMagicComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        size = 0.5F
                                    }
                                    .addParticleControlerInstanceInit {
                                        var time: Float = 0.0F
                                        addPreTickAction {
                                            if (this@MeteoriteMagicComposition.status.isEnable()) {
                                                time++
                                                particle.particleAlpha = (time / 20).toFloat()
                                            }
                                        }
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(2.354911, 1.255872, 0.0),
                                RelativeLocation(-7.611607, 0.281553, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, -20.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addFourierSeries(
                                        FourierSeriesBuilder()
                                            .count(180 * option)
                                            .scale(12 / 5.0)
                                            .addFourier(2.0, 3.0, 0.0)
                                            .addFourier(3.0, -2.0, 0.0)
                                    )
                                    .addDiscreteCircleXZ(12.0, 120, 0.4)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.3F
                                        color = this@MeteoriteMagicComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                                    .addParticleControlerInstanceInit {
                                        var time: Float = 0.0F
                                        addPreTickAction {
                                            if (this@MeteoriteMagicComposition.status.isEnable()) {
                                                time++
                                                particle.particleAlpha = (time / 20).toFloat()
                                            }
                                        }
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 64)
                                }
                            }

                        }
                    )
                }
        ] = RelativeLocation(0.0, -35.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(18.0, 120, 0.4)
                                    .addPolygonInCircle(3, 100, 18.0)
                                    .rotateAsAxis(0.333333 * PI)
                                    .addPolygonInCircle(3, 100, 18.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        color = this@MeteoriteMagicComposition.color
                                        size = 0.4F
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                                    .addParticleControlerInstanceInit {
                                        var time: Float = 0.0F
                                        addPreTickAction {
                                            if (this@MeteoriteMagicComposition.status.isEnable()) {
                                                time++
                                                particle.particleAlpha = (time / 20).toFloat()
                                            }
                                        }
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 128)
                                }
                            }

                        }
                    )
                }
        ] = RelativeLocation(0.0, 50.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            repeat(6) { index ->
                                val finalAngle = (2.0 * PI) * (index / 6.0)
                                applyPoint(RelativeLocation(0.0, 0.0, 0.0)) { shapeRel1 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation.yAxis()
                                                    applyBuilder(
                                                        PointsBuilder()
                                                            .addFillTriangle(
                                                                RelativeLocation(0.919508, 0.0, 39.621212),
                                                                RelativeLocation(-0.955492, 0.0, 39.621212),
                                                                RelativeLocation(0.044508, 0.0, 39.121212),
                                                                option * 2
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.002841, 0.0, 39.079545),
                                                                RelativeLocation(0.653883, 0.0, 37.558712),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.002841, 0.0, 39.079545),
                                                                RelativeLocation(-0.721117, 0.0, 37.558712),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.653883, 0.0, 37.558712),
                                                                RelativeLocation(3.002841, 0.0, 37.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.721117, 0.0, 37.558712),
                                                                RelativeLocation(-2.997159, 0.0, 37.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.502841, 0.0, 36.579545),
                                                                RelativeLocation(2.002841, 0.0, 36.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.497159, 0.0, 36.579545),
                                                                RelativeLocation(-1.872159, 0.0, 36.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.002841, 0.0, 37.579545),
                                                                RelativeLocation(0.002841, 0.0, 35.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.919508, 0.0, 39.621212),
                                                                RelativeLocation(2.002841, 0.0, 38.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.955492, 0.0, 39.621212),
                                                                RelativeLocation(-1.997159, 0.0, 38.079545),
                                                                option * 7
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.002841, 0.0, 40.079545),
                                                                RelativeLocation(0.002841, 0.0, 41.079545),
                                                                option * 7
                                                            )
                                                    ) { shapeRel2 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                                            }
                                                            .addParticleInstanceInit {
                                                                color = this@MeteoriteMagicComposition.color
                                                                size = 0.3F
                                                                textureSheet =
                                                                    CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                                            }
                                                    }
                                                    applyDisplayAction {
                                                        val animator = AngleAnimator(20, finalAngle, Eases.outExpo)
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
                                                            rotateToWithAngle(direction, -PI / 64)
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
        ] = RelativeLocation(0.0, 52.0, 0.0)

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
            if (age++ > 60) {
                scaleHelper.doScale()
            }
            rotateToPoint(direction)
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            }
        }
    }
}
