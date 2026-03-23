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
import java.util.SortedMap
import java.util.TreeMap
import org.joml.Vector3f

@CooAutoRegister
class SwordFormationMagicChargingComposition(position: Vec3, world: Level? = null) :
    AutoSequencedParticleComposition(position, world) {
    @CodecField
    var direction: Vec3 = Vec3(0.0, 0.0, 1.0)

    @CodecField
    var time: Int = 0

    init {
        axis = RelativeLocation.yAxis()
        setDisabledInterval(10)
        animate.addAnimate(5) { time > 1 }
            .addAnimate(6) { time > 3 }
            .addAnimate(1) { time > 6 }
            .addAnimate(1) { time > 8 }
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val result: SortedMap<CompositionData, RelativeLocation> = TreeMap()
        var orderCounter = 0

        val angleOffsetCount1 = 5
        repeat(angleOffsetCount1) { index ->
            val finalAngle1 = (2.0 * PI) * index.toDouble() / angleOffsetCount1.toDouble()
            result[
                CompositionData().apply { order = orderCounter++ }
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                loadScaleHelperBezierValue(
                                    0.01,
                                    1.0,
                                    10,
                                    RelativeLocation(5.511707, 1.407432, 0.0),
                                    RelativeLocation(5.511707, 1.422319, 0.0)
                                )
                                applyBuilder(
                                    PointsBuilder()
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 7.102679),
                                            RelativeLocation(0.5, 0.0, 6.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 7.102679),
                                            RelativeLocation(-0.5, 0.0, 6.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.5, 0.0, 6.602679),
                                            RelativeLocation(0.5, 0.0, 3.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.5, 0.0, 6.602679),
                                            RelativeLocation(-0.5, 0.0, 3.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 7.102679),
                                            RelativeLocation(0.0, 0.0, 4.227679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.5, 0.0, 3.727679),
                                            RelativeLocation(1.875, 0.0, 3.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.5, 0.0, 3.727679),
                                            RelativeLocation(-1.875, 0.0, 3.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(1.875, 0.0, 3.727679),
                                            RelativeLocation(1.5, 0.0, 3.352679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-1.875, 0.0, 3.727679),
                                            RelativeLocation(-1.5, 0.0, 3.352679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(1.5, 0.0, 3.352679),
                                            RelativeLocation(0.25, 0.0, 3.352679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-1.5, 0.0, 3.352679),
                                            RelativeLocation(-0.25, 0.0, 3.352679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.25, 0.0, 3.352679),
                                            RelativeLocation(0.25, 0.0, 1.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.25, 0.0, 3.352679),
                                            RelativeLocation(-0.25, 0.0, 1.727679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.25, 0.0, 1.727679),
                                            RelativeLocation(-0.25, 0.0, 1.727679),
                                            30
                                        )
                                ) { shapeRel0 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                        }
                                        .addParticleInstanceInit {
                                            color = Vector3f(128f / 255, 213f / 255, 254f / 255)
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(20, finalAngle1, Eases.outBack)
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                        .step {
                                            if (!this@SwordFormationMagicChargingComposition.status.isDisable()) return@step false
                                            rotateAsAxis(animator.fadeDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                    }
                                    addPreTickAction {
                                        rotateToWithAngle(direction.asRelative(), 0.015915 * PI)
                                    }
                                    setReversedScaleOnCompositionStatus(this@SwordFormationMagicChargingComposition)
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 3.5, 0.0)
        }

        val angleOffsetCount2 = 6
        repeat(angleOffsetCount2) { index ->
            val finalAngle2 = (-2.0 * PI) * index.toDouble() / angleOffsetCount2.toDouble()
            result[
                CompositionData().apply { order = orderCounter++ }
                    .setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            ParticleShapeComposition(it).apply {
                                axis = RelativeLocation.yAxis()
                                loadScaleHelperBezierValue(
                                    0.01,
                                    1.0,
                                    10,
                                    RelativeLocation(5.012263, 1.114296, 0.0),
                                    RelativeLocation(5.047938, 1.129705, 0.0)
                                )
                                applyBuilder(
                                    PointsBuilder()
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 11.977679),
                                            RelativeLocation(0.5, 0.0, 11.477679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 11.977679),
                                            RelativeLocation(-0.5, 0.0, 11.477679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.5, 0.0, 11.477679),
                                            RelativeLocation(0.5, 0.0, 8.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.5, 0.0, 11.477679),
                                            RelativeLocation(-0.5, 0.0, 8.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.0, 0.0, 11.977679),
                                            RelativeLocation(0.0, 0.0, 9.102679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.5, 0.0, 8.602679),
                                            RelativeLocation(1.875, 0.0, 8.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.5, 0.0, 8.602679),
                                            RelativeLocation(-1.875, 0.0, 8.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(1.875, 0.0, 8.602679),
                                            RelativeLocation(1.5, 0.0, 8.227679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-1.875, 0.0, 8.602679),
                                            RelativeLocation(-1.5, 0.0, 8.227679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(1.5, 0.0, 8.227679),
                                            RelativeLocation(0.25, 0.0, 8.227679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-1.5, 0.0, 8.227679),
                                            RelativeLocation(-0.25, 0.0, 8.227679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.25, 0.0, 8.227679),
                                            RelativeLocation(0.25, 0.0, 6.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(-0.25, 0.0, 8.227679),
                                            RelativeLocation(-0.25, 0.0, 6.602679),
                                            30
                                        )
                                        .addLine(
                                            RelativeLocation(0.25, 0.0, 6.602679),
                                            RelativeLocation(-0.25, 0.0, 6.602679),
                                            30
                                        )
                                ) { shapeRel0 ->
                                    CompositionData()
                                        .setDisplayerSupplier {
                                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                        }
                                        .addParticleInstanceInit {
                                            color = Vector3f(128f / 255, 213f / 255, 254f / 255)
                                        }
                                }
                                applyDisplayAction {
                                    val animator = AngleAnimator(10, finalAngle2, Eases.linear)
                                    animator.reset()
                                    val timeline = Timeline()
                                        .step {
                                            rotateAsAxis(animator.glowDelta())
                                            animator.finished
                                        }
                                        .step {
                                            if (!this@SwordFormationMagicChargingComposition.status.isDisable()) return@step false
                                            rotateAsAxis(animator.fadeDelta())
                                            animator.finished
                                        }
                                    addPreTickAction {
                                        timeline.doTick()
                                    }
                                    addPreTickAction {
                                        rotateToWithAngle(direction.asRelative(), -0.031831 * PI)
                                    }
                                    setReversedScaleOnCompositionStatus(this@SwordFormationMagicChargingComposition)
                                }
                            }
                        )
                    }
            ] = RelativeLocation(0.0, 4.2, 0.0)
        }

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.736901, 1.0, 0.0),
                                RelativeLocation(4.236343, 1.203394, 0.0)
                            )
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(2.0, 120)
                                    .addPolygonInCircle(3, 30, 2.0)
                                    .rotateAsAxis(0.333333 * PI)
                                    .addPolygonInCircle(3, 30, 2.0)
                            ) { shapeRel0 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        color = Vector3f(0.792157F, 0.545098F, 0.996078F)
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction.asRelative(), -0.055556 * PI)
                                }
                                setReversedScaleOnCompositionStatus(this@SwordFormationMagicChargingComposition)
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 3.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            loadScaleHelperBezierValue(
                                0.01,
                                1.5,
                                10,
                                RelativeLocation(3.888517, 1.769274, 0.0),
                                RelativeLocation(3.906354, 1.756395, 0.0)
                            )
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(6.0, 240)
                                    .addLine(RelativeLocation(5.375, 0.0, -4.0), RelativeLocation(5.25, 0.0, -3.5), 8)
                                    .addLine(RelativeLocation(5.0, 0.0, -4.125), RelativeLocation(5.5, 0.0, -3.5), 8)
                                    .addLine(RelativeLocation(1.75, 0.0, -6.5), RelativeLocation(2.25, 0.0, -5.875), 8)
                                    .addLine(RelativeLocation(2.0, 0.0, -6.125), RelativeLocation(2.375, 0.0, -6.25), 8)
                                    .addLine(
                                        RelativeLocation(1.75, 0.0, -6.375),
                                        RelativeLocation(2.375, 0.0, -6.25),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-1.875, 0.0, -6.25),
                                        RelativeLocation(-2.5, 0.0, -5.75),
                                        8
                                    )
                                    .addLine(RelativeLocation(-2.0, 0.0, -6.25), RelativeLocation(-1.75, 0.0, -6.0), 8)
                                    .addLine(
                                        RelativeLocation(-2.25, 0.0, -6.25),
                                        RelativeLocation(-1.625, 0.0, -6.5),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-4.875, 0.0, -4.0),
                                        RelativeLocation(-5.375, 0.0, -3.25),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-5.125, 0.0, -4.25),
                                        RelativeLocation(-4.875, 0.0, -4.0),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-5.5, 0.0, -3.625),
                                        RelativeLocation(-5.125, 0.0, -4.25),
                                        8
                                    )
                                    .addLine(RelativeLocation(-5.5, 0.0, -4.25), RelativeLocation(-5.5, 0.0, -3.625), 8)
                                    .addLine(
                                        RelativeLocation(-6.5, 0.0, -0.875),
                                        RelativeLocation(-6.375, 0.0, 0.875),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-6.875, 0.0, -0.25),
                                        RelativeLocation(-6.0, 0.0, -0.25),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-6.875, 0.0, -0.25),
                                        RelativeLocation(-6.0, 0.0, 0.625),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-5.5, 0.0, 3.375),
                                        RelativeLocation(-6.125, 0.0, 3.375),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-5.25861, 0.0, 3.820604),
                                        RelativeLocation(-5.5, 0.0, 3.375),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-5.25861, 0.0, 3.820604),
                                        RelativeLocation(-4.5, 0.0, 4.0),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-2.00861, 0.0, 6.181867),
                                        RelativeLocation(-2.5, 0.0, 6.25),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-1.516954, 0.0, 6.489396),
                                        RelativeLocation(-1.0, 0.0, 6.5),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(-2.00861, 0.0, 6.181867),
                                        RelativeLocation(-1.5, 0.0, 6.5),
                                        8
                                    )
                                    .addCircle(7.0, 240)
                                    .addLine(RelativeLocation(6.5, 0.0, 0.0), RelativeLocation(6.625, 0.0, -0.375), 8)
                                    .addLine(RelativeLocation(6.5, 0.0, 0.125), RelativeLocation(6.25, 0.0, -0.375), 8)
                                    .addLine(RelativeLocation(5.5, 0.0, 3.75), RelativeLocation(5.75, 0.0, 3.375), 8)
                                    .addLine(
                                        RelativeLocation(5.708333, 0.0, 3.4375),
                                        RelativeLocation(5.375, 0.0, 3.25),
                                        8
                                    )
                                    .addLine(RelativeLocation(5.375, 0.0, 3.25), RelativeLocation(5.25, 0.0, 3.875), 8)
                                    .addLine(
                                        RelativeLocation(2.00861, 0.0, 6.181867),
                                        RelativeLocation(2.00861, 0.0, 6.181867),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(2.00861, 0.0, 6.181867),
                                        RelativeLocation(1.75, 0.0, 6.25),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(2.00861, 0.0, 6.181867),
                                        RelativeLocation(2.25, 0.0, 6.0),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(2.129305, 0.0, 6.090934),
                                        RelativeLocation(1.988107, 0.0, 6.711738),
                                        8
                                    )
                                    .addLine(
                                        RelativeLocation(1.75, 0.0, 6.25),
                                        RelativeLocation(1.455382, 0.0, 6.847033),
                                        8
                                    )
                            ) { shapeRel0 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.4F
                                        color = Vector3f(0.8F, 0.568627F, 0.992157F)
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction.asRelative(), 0.015915 * PI)
                                }
                                setReversedScaleOnCompositionStatus(this@SwordFormationMagicChargingComposition)
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 2.0, 0.0)

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
            rotateToPoint(direction.asRelative())
            time++
        }
    }
}