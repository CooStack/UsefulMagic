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
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet

@CooAutoRegister
class AntiEntityChargingComposition(position: Vec3, world: Level? = null) : AutoSequencedParticleComposition(position, world) {
    @CodecField
    var age: Int = 0

    @CodecField
    var color: Vector3f = Vector3f(0.662745F, 0.541176F, 1F)

    val option: Int = 3

    private val scaleHelper = CompositionBezierScaleHelper(10, 0.01, 4.0, RelativeLocation(3.408578, 4.501318, 0.0), RelativeLocation(3.386005, 4.571992, 0.0))

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)
    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
        setDisabledInterval(10)
        animate.addAnimate(1) { age > 0 }
            .addAnimate(5) { age > 50 }
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
                                    .addCircle(12.0, 120*option)
                                    .addCircle(8.5, 120*option)
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(10, 10.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .addLine(RelativeLocation(1.0, 0.0, 1.0), RelativeLocation(2.0, 0.0, 0.0), 30)
                                                    .addLine(RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(1.0, 0.0, -1.0), 30)
                                                    .addLine(RelativeLocation(1.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, 1.0), 30)
                                                    .addLine(RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(1.0, 0.0, 0.0), 30)
                                                p.rotateTo(it)
                                                res.addAll(p
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
                                        color = this@AntiEntityChargingComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        size = 0.3F
                                    }
                            }
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(14.0, 50)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.8F
                                        color = this@AntiEntityChargingComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        currentAge = Random.nextInt(lifetime)
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.469526, 1.264708, 0.0), RelativeLocation(-7.24605, 0.029849, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI/64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        val angleOffsetCount2 = 4
        repeat(angleOffsetCount2) { index ->
            val finalAngle2 = (-2.0*PI) * (index / 4.0)
            result[
                CompositionData().apply { order = orderCounter++ }
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                applyPoint(RelativeLocation(0.0, 0.0, 5.2)) { shapeRel1 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(it).apply {
                                                    axis = RelativeLocation(0.0, 1.0, 0.0)
                                                    applyBuilder(
                                                        PointsBuilder()
                                                            .addCircle(3.0, 120)
                                                            .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                                                PointsBuilder()
                                                                    .addPolygonInCircle(3, 10*option, 3.0)
                                                                    .rotateAsAxis(-0.333333*PI, RelativeLocation(0.0, 1.0, 0.0))
                                                            )
                                                            .addPolygonInCircle(3, 10*option, 3.0)
                                                    ) { shapeRel2 ->
                                                        CompositionData()
                                                            .setDisplayerSupplier {
                                                                ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                                            }
                                                            .addParticleInstanceInit {
                                                                color = this@AntiEntityChargingComposition.color
                                                                textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                                                size = 0.3F
                                                            }
                                                    }
                                                    loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.051919, 0.991057, 0.0), RelativeLocation(-6.072235, -0.029333, 0.0))
                                                }
                                            )
                                        }
                                }
                                loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(5.180587, 0.909884, 0.0), RelativeLocation(-4.717833, -0.054771, 0.0))
                                applyDisplayAction {
                                    val animator = AngleAnimator(20, finalAngle2, Eases.bezierEase(RelativeLocation(0.755079, 1.0, 0.0), RelativeLocation(-0.243792, 0.0, 0.0)))
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
                                        rotateAsAxis(-PI/48)
                                    }
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 0.0, 0.0)
        }

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(4, 15*option, 5.2)
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(4, 15*option, 5.2)
                                            .rotateAsAxis(0.25*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        color = this@AntiEntityChargingComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(5.180587, 0.909884, 0.0), RelativeLocation(-4.717833, -0.054771, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(-PI/48)
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
            scaleHelper.doScale()
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
            age++
            toggleRelative()
        }
    }
}