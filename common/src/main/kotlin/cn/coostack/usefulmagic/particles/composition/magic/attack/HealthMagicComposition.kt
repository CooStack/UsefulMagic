package cn.coostack.usefulmagic.particles.composition.magic.attack

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
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
import java.util.UUID
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder


@CooAutoRegister
class HealthMagicComposition(position: Vec3, world: Level? = null) : AutoSequencedParticleComposition(position, world) {
    @CodecField
    var age: Int = 0

    @CodecField
    var color: Vector3f = Vector3f(0.72549F, 1F, 0.580392F)

    val option: Int = 3

    init {
        axis = RelativeLocation.yAxis()
        setDisabledInterval(10)
        animate.addAnimate(3) { age > 0 }
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
                            applyPoint(RelativeLocation(0.0, 0.0, 0.0)) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withComposition(
                                            ParticleShapeComposition(it).apply {
                                                axis = RelativeLocation.yAxis()
                                                applyBuilder(
                                                    PointsBuilder()
                                                        .addCircle(16.0, 240 * option)
                                                        .addCircle(18.0, 240 * option)
                                                        .addCircle(10.0, 120 * option)
                                                        .addBuilder(
                                                            RelativeLocation(0.5, 0.0, 4.875),
                                                            PointsBuilder()
                                                                .addCircle(5.0, 120 * option)
                                                                .addBuilder(
                                                                    RelativeLocation(0.0, 0.0, 0.0),
                                                                    PointsBuilder()
                                                                        .addPolygonInCircle(3, 30 * option, 5.0)
                                                                        .rotateAsAxis(
                                                                            0.333333 * PI,
                                                                            RelativeLocation(0.0, 1.0, 0.0)
                                                                        )
                                                                )
                                                                .addPolygonInCircle(3, 30 * option, 5.0)
                                                        )
                                                        .addBuilder(
                                                            RelativeLocation(7.0, 0.0, 0.0),
                                                            PointsBuilder()
                                                                .addFourierSeries(
                                                                    FourierSeriesBuilder()
                                                                        .count(120 * option)
                                                                        .scale(1.0)
                                                                        .addFourier(2.0, 1.0, 0.0)
                                                                        .addFourier(1.0, -1.0, 180.0)
                                                                )
                                                                .addBuilder(
                                                                    RelativeLocation(0.0, 0.0, 0.0),
                                                                    PointsBuilder()
                                                                        .addFourierSeries(
                                                                            FourierSeriesBuilder()
                                                                                .count(120 * option)
                                                                                .scale(0.5)
                                                                                .addFourier(0.5, 1.0, 0.0)
                                                                                .addFourier(1.0, -1.0, 0.0)
                                                                        )
                                                                )
                                                        )
                                                        .addLine(
                                                            RelativeLocation(6.375, 0.0, 0.0),
                                                            RelativeLocation(7.625, 0.0, 0.0),
                                                            10 * option
                                                        )
                                                        .addBuilder(
                                                            RelativeLocation(0.0, 0.0, 0.0),
                                                            PointsBuilder()
                                                                .addBuilder(
                                                                    RelativeLocation(0.0, 0.0, 0.0),
                                                                    PointsBuilder()
                                                                        .addPolygonInCircle(4, 100 * option, 16.0)
                                                                        .rotateAsAxis(
                                                                            (PI / 4),
                                                                            RelativeLocation(0.0, 1.0, 0.0)
                                                                        )
                                                                )
                                                                .addPolygonInCircle(4, 100 * option, 16.0)
                                                        )
                                                        .addBuilder(
                                                            RelativeLocation(7.0, 0.0, 0.0),
                                                            PointsBuilder()
                                                                .addCircle(3.0, 80 * option)
                                                        )
                                                        .addLine(
                                                            RelativeLocation(-8.0, 0.0, -1.125),
                                                            RelativeLocation(-5.0, 0.0, -1.0),
                                                            option * 20
                                                        )
                                                        .addLine(
                                                            RelativeLocation(0.375, 0.0, 5.0),
                                                            RelativeLocation(-3.125, 0.0, -6.625),
                                                            option * 30
                                                        )
                                                        .addLine(
                                                            RelativeLocation(0.125, 0.0, -3.0),
                                                            RelativeLocation(5.0, 0.0, -5.0),
                                                            option * 20
                                                        )
                                                        .addLine(
                                                            RelativeLocation(-3.125, 0.0, -6.625),
                                                            RelativeLocation(5.0, 0.0, -5.0),
                                                            option * 20
                                                        )
                                                        .addBuilder(
                                                            RelativeLocation(-5.0, 0.0, -1.0),
                                                            PointsBuilder()
                                                                .addCircle(0.5, 120)
                                                        )
                                                        .addLine(
                                                            RelativeLocation(-3.125, 0.0, -6.625),
                                                            RelativeLocation(-8.0, 0.0, -1.125),
                                                            option * 20
                                                        )
                                                ) { shapeRel2 ->
                                                    CompositionData()
                                                        .setDisplayerSupplier {
                                                            ParticleDisplayer.withCParticle(it)
                                                        }
                                                        .addCParticleInstanceInit {
                                                            effect = ControlableEndRodEffect(UUID.randomUUID())
                                                            color = this@HealthMagicComposition.color
                                                        }
                                                }
                                                loadScaleHelperBezierValue(
                                                    0.01,
                                                    1.0,
                                                    10,
                                                    RelativeLocation(4.604966, 1.130154, 0.0),
                                                    RelativeLocation(-5.316027, 0.14411, 0.0)
                                                )
                                                applyDisplayAction {
                                                    addPreTickAction {
                                                        rotateAsAxis(0.015915 * PI)
                                                    }
                                                    setReversedScaleOnCompositionStatus(this@HealthMagicComposition)
                                                }
                                            }
                                        )
                                    }
                            }
                            applyPoint(RelativeLocation(0.0, 0.0, 0.0)) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withComposition(
                                            ParticleShapeComposition(it).apply {
                                                axis = RelativeLocation.yAxis()
                                                applyBuilder(
                                                    PointsBuilder()
                                                        .addCircle(17.0, 80)
                                                ) { shapeRel2 ->
                                                    CompositionData()
                                                        .setDisplayerSupplier {
                                                            ParticleDisplayer.withCParticle(it)
                                                        }
                                                        .addCParticleInstanceInit {
                                                            effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                                            size = 0.8F
                                                            age = Random.nextInt(maxAge)
                                                            color = this@HealthMagicComposition.color
                                                        }
                                                }
                                                loadScaleHelperBezierValue(
                                                    0.01,
                                                    1.0,
                                                    10,
                                                    RelativeLocation(5.677201, 1.129446, 0.0),
                                                    RelativeLocation(-4.334086, 0.101942, 0.0)
                                                )
                                                applyDisplayAction {
                                                    addPreTickAction {
                                                        rotateAsAxis(-PI / 64)
                                                    }
                                                    setReversedScaleOnCompositionStatus(this@HealthMagicComposition)
                                                }
                                            }
                                        )
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
        var syncedAnimationIndex = animate.animationIndex
        addPreTickAction {
            age++
            if (!client && syncedAnimationIndex != animate.animationIndex) {
                syncedAnimationIndex = animate.animationIndex
                markDirty()
            }
            toggleRelative()
        }
    }
}
