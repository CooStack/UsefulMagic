package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.animation.timeline.*
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
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

@CooAutoRegister
class StarryChargingComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(1F, 0.333333F, 0.258824F)

    val option: Int = 3
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
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(12.0, 120*option)
                                    .addCircle(14.0, 160*option)
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(3, 5.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                    .addBezierCurve(RelativeLocation(1.0, 0.0, 5.0), RelativeLocation(-1.0, 0.0, 5.0), RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(0.0, 0.0, 1.0), 30*option)
                                                    .addBezierCurve(RelativeLocation(1.0, 0.0, 5.0), RelativeLocation(2.0, 0.0, 6.0), RelativeLocation(-1.0, 0.0, 2.0), RelativeLocation(-2.0, 0.0, 1.0), 30*option)
                                                    .addBezierCurve(RelativeLocation(-1.0, 0.0, 5.0), RelativeLocation(-2.0, 0.0, 6.0), RelativeLocation(1.0, 0.0, 2.0), RelativeLocation(2.0, 0.0, 1.0), 30*option)
                                                    .addBezierCurve(RelativeLocation(1.0, 0.0, 5.0), RelativeLocation(2.0, 0.0, 6.0), RelativeLocation(-1.0, 0.0, 2.0), RelativeLocation(-2.0, 0.0, 1.0), 30*option)
                                                    .addBezierCurve(RelativeLocation(2.0, 0.0, 4.0), RelativeLocation(-2.0, 0.0, 4.0), RelativeLocation(-4.0, 0.0, 4.0), RelativeLocation(4.0, 0.0, 4.0), 40*option)
                                                    .addBezierCurve(RelativeLocation(0.0, 0.0, 5.0), RelativeLocation(2.0, 0.0, 3.0), RelativeLocation(0.0, 0.0, -2.0), RelativeLocation(-1.0, 0.0, 1.125), 30*option)
                                                    .addBezierCurve(RelativeLocation(0.0, 0.0, 5.0), RelativeLocation(-2.0, 0.0, 3.0), RelativeLocation(0.0, 0.0, -2.0), RelativeLocation(1.0, 0.0, 1.125), 30*option)
                                                    .addLine(RelativeLocation(0.0, 0.0, 5.0), RelativeLocation(0.0, 0.0, 2.0), 30)
                                                p.rotateTo(it)
                                                res.addAll(p
                                                    .pointsOnEach { rel -> rel.add(it) }
                                                    .createWithoutClone()
                                                )
                                            }
                                        res
                                    }
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addWith {
                                                val res = arrayListOf<RelativeLocation>()
                                                getPolygonInCircleVertices(3, 7.0)
                                                    .forEach { it ->
                                                        val p = PointsBuilder()
                                                            .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                            .addBezierCurve(RelativeLocation(2.0, 0.0, 3.0), RelativeLocation(-2.0, 0.0, 3.0), RelativeLocation(0.0, 0.0, -2.0), RelativeLocation(0.0, 0.0, 2.0), 100)
                                                            .addBezierCurve(RelativeLocation(2.0, 0.0, 3.0), RelativeLocation(-2.0, 0.0, 3.0), RelativeLocation(0.0, 0.0, 2.0), RelativeLocation(0.0, 0.0, -2.0), 100)
                                                            .addBezierCurve(RelativeLocation(2.0, 0.0, 4.0), RelativeLocation(-2.0, 0.0, 4.0), RelativeLocation(-5.0, 0.0, -2.0), RelativeLocation(5.0, 0.0, -2.0), 100)
                                                            .addBezierCurve(RelativeLocation(2.0, 0.0, 2.0), RelativeLocation(-2.0, 0.0, 2.0), RelativeLocation(-5.0, 0.0, 3.0), RelativeLocation(5.0, 0.0, 3.0), 100)
                                                            .addLine(RelativeLocation(0.0, 0.0, 4.0), RelativeLocation(0.0, 0.0, 1.0), 30)
                                                            .addBezierCurve(RelativeLocation(1.0, 0.0, 2.0), RelativeLocation(-1.0, 0.0, 2.0), RelativeLocation(-1.0, 0.0, -1.0), RelativeLocation(1.0, 0.0, -1.0), 100)
                                                        p.rotateTo(it)
                                                        res.addAll(p
                                                            .pointsOnEach { rel -> rel.add(it) }
                                                            .createWithoutClone()
                                                        )
                                                    }
                                                res
                                            }
                                            .rotateAsAxis(0.333333*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 0.3F
                                        color = this@StarryChargingComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(13.0, 60)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 1.2F
                                        color = this@StarryChargingComposition.color
                                        currentAge = Random.nextInt(lifetime)
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.119639, 1.233565, 0.0), RelativeLocation(-5.891648, 0.260753, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI/64)
                                }
                                setReversedScaleOnCompositionStatus(this@StarryChargingComposition)
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
                                    .addCircle(6.0, 70*option)
                                    .addPolygonInCircle(4, 15*option, 6.0)
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(4, 15*option, 6.0)
                                            .rotateAsAxis(-0.25*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        color = this@StarryChargingComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.119639, 1.233565, 0.0), RelativeLocation(-5.891648, 0.260753, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI/48)
                                }
                                setReversedScaleOnCompositionStatus(this@StarryChargingComposition)
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
            rotateAsAxis(PI/64)
        }
    }
}