package cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class MagicDragonSpawnLaserComposition(position: Vec3, world: Level? = null) :
    AutoParticleComposition(position, world) {
    @CodecField
    var direction: RelativeLocation = RelativeLocation(0.0, 1.0, 0.0)

    @CodecField
    var color: Vector3f = Vector3f(0.729412F, 0.4F, 1F)

    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        1.0,
        RelativeLocation(3.837472, 1.165466, 0.0),
        RelativeLocation(3.848758, 1.119329, 0.0)
    )

    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
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
                                    .addCircle(8.0, 300)
                                    .clearAsBallMask(RelativeLocation(2.5, 0.0, 4.330127), 0.95)
                                    .addPolygonInCircle(6, 45, 5.5)
                                    .addPolygonInCircle(6, 45, 5.0)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(3, 100, 8.0)
                                            .rotateAsAxis(0.833333 * PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .clearAsRoundXZMask(RelativeLocation(-2.5, 0.0, 4.330127), 0.9, -1.0)
                                    .clearAsRoundXZMask(RelativeLocation(-5.0, 0.0, 0.0), 0.9, -1.0)
                                    .clearAsRoundXZMask(RelativeLocation(-2.5, 0.0, -4.330127), 0.9, -1.0)
                                    .clearAsRoundXZMask(RelativeLocation(2.5, 0.0, -4.330127), 0.9, -1.0)
                                    .clearAsRoundXZMask(RelativeLocation(5.0, 0.0, 0.0), 0.9, -1.0)
                                    .clearAsRoundXZMask(RelativeLocation(2.5, 0.0, 4.330127), 0.9, -1.0)
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(2.5, 0.0, 4.330127),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(-2.5, 0.0, -4.330127)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(-2.5, 0.0, -4.330127))
                                            )
                                            .addBuilder(
                                                RelativeLocation(-2.5, 0.0, 4.330127),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(2.5, 0.0, -4.330127)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(2.5, 0.0, -4.330127))
                                            )
                                            .addBuilder(
                                                RelativeLocation(-5.0, 0.0, 0.0),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(5.0, 0.0, 0.0)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(5.0, 0.0, 0.0))
                                            )
                                            .addBuilder(
                                                RelativeLocation(-2.5, 0.0, -4.330127),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(2.5, 0.0, 4.330127)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(2.5, 0.0, 4.330127))
                                            )
                                            .addBuilder(
                                                RelativeLocation(2.5, 0.0, -4.330127),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(-2.5, 0.0, 4.330127)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(-2.5, 0.0, 4.330127))
                                            )
                                            .addBuilder(
                                                RelativeLocation(5.0, 0.0, 0.0),
                                                PointsBuilder()
                                                    .addCircle(1.0, 60)
                                                    .addPolygonInCircle(3, 15, 1.0)
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 15, 1.0)
                                                            .rotateAsAxis(
                                                                -0.333333 * PI,
                                                                RelativeLocation(0.0, 1.0, 0.0)
                                                            )
                                                    )
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        RelativeLocation(-5.0, 0.0, 0.0)
                                                    )
                                                    .rotateAsAxis(0.0, RelativeLocation(-5.0, 0.0, 0.0))
                                            )
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addBuilder(
                                                RelativeLocation(5.196152, 0.0, 3.0),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                RelativeLocation(0.0, 0.0, -1.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 1.0),
                                                                RelativeLocation(-0.75, 0.0, 0.5),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.75, 0.0, 0.5),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                RelativeLocation(-0.75, 0.0, -0.625),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.75, 0.0, -0.625),
                                                                RelativeLocation(0.0, 0.0, -1.0),
                                                                10
                                                            )
                                                            .scale(0.7)
                                                    )
                                                    .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(RelativeLocation(5.196152, 0.0, 3.0))
                                            )
                                            .addBuilder(
                                                RelativeLocation(-5.196152, 0.0, 3.0),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addCircle(RelativeLocation(0.0, 0.0, 0.916667), 0.5, 30)
                                                            .addLine(
                                                                RelativeLocation(0.0, 0.0, 0.291667),
                                                                RelativeLocation(0.0, 0.0, -0.75),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.375, 0.0, -0.125),
                                                                RelativeLocation(-0.375, 0.0, -0.125),
                                                                10
                                                            )
                                                            .addRadian(
                                                                RelativeLocation(0.004853, 0.0, 1.944586),
                                                                0.5,
                                                                10,
                                                                0.0,
                                                                1.0 * PI,
                                                                1.0 * PI
                                                            )
                                                            .scale(0.7)
                                                    )
                                                    .pointsOnEach { it.add(0.0, 0.0, -0.375) }
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(RelativeLocation(-5.196152, 0.0, 3.0))
                                            )
                                            .addBuilder(
                                                RelativeLocation(0.0, 0.0, -6.0),
                                                PointsBuilder()
                                                    .addBuilder(
                                                        RelativeLocation(0.0, 0.0, 0.0),
                                                        PointsBuilder()
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, -1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, -1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, 1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, 1.0),
                                                                RelativeLocation(0.0, 0.0, 0.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(0.5, 0.0, 1.0),
                                                                RelativeLocation(0.5, 0.0, -1.0),
                                                                10
                                                            )
                                                            .addLine(
                                                                RelativeLocation(-0.5, 0.0, 1.0),
                                                                RelativeLocation(-0.5, 0.0, -1.0),
                                                                10
                                                            )
                                                            .scale(0.7)
                                                    )
                                                    .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .rotateTo(RelativeLocation(0.0, 0.0, -6.0))
                                            )
                                            .scale(1.1)
                                    )
                                    .addCircle(10.0, 360)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        size = 0.18F
                                        color = this@MagicDragonSpawnLaserComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.837472, 1.155466, 0.0),
                                RelativeLocation(-6.151242, 0.119329, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 72)
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
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(9.0, 50)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                        size = 0.5F
                                        color = this@MagicDragonSpawnLaserComposition.color
                                        age = Random.nextInt(maxAge)
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(3.837472, 1.155466, 0.0),
                                RelativeLocation(-6.151242, 0.119329, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, 0.015915 * PI)
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
                playCParticleAlphaTransition(10f, CParticleCurve.linear(1f, 0f))
            }
            toggleRelative()
        }
    }
}
