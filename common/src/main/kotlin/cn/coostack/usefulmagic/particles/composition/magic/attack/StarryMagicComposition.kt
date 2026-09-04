package cn.coostack.usefulmagic.particles.composition.magic.attack

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
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
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder

@CooAutoRegister
class StarryMagicComposition(position: Vec3, world: Level? = null) : AutoSequencedParticleComposition(position, world) {
    @CodecField
    var directon: RelativeLocation = RelativeLocation(0.0, 1.0, 0.0)

    @CodecField
    var color: Vector3f = Vector3f(0.94902F, 0.431373F, 0.431373F)

    @CodecField
    var age: Int = 0

    val option: Int = 3

    private val scaleHelper = CompositionBezierScaleHelper(10, 0.01, 1.0, RelativeLocation(4.909707, 1.083422, 0.0), RelativeLocation(4.954853, 1.074934, 0.0))

    init {
        axis = RelativeLocation(0.0, -1.0, 0.0)
        scaleHelper.loadControler(this)
        setDisabledInterval(20)
        animate.addAnimate(1) { age > 1 }
            .addAnimate(5) { age > 70 }
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
                                    .addCircle(20.0, 240*option)
                                    .addCircle(18.0, 240*option)
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(3, 100*option, 27.0)
                                            .addPolygonInCircle(3, 100*option, 24.0)
                                            .rotateAsAxis(-0.5*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addBuilder(RelativeLocation(0.0, 0.0, 4.0),
                                        PointsBuilder()
                                            .addFourierSeries(
                                                FourierSeriesBuilder()
                                                    .count(360)
                                                    .scale(1.0)
                                                    .addFourier(3.0, 1.0, 0.0)
                                                    .addFourier(8.0, -1.0, 0.0)
                                            )
                                    )
                                    .addBuilder(RelativeLocation(0.0, 0.0, 4.0),
                                        PointsBuilder()
                                            .addCircle(0.8, 35*option)
                                    )
                                    .addBuilder(RelativeLocation(0.0, 0.0, 4.0),
                                        PointsBuilder()
                                            .addCircle(1.4, 30*option)
                                    )
                                    .addBuilder(RelativeLocation(0.0, 0.0, 4.0),
                                        PointsBuilder()
                                            .addCircle(3.0, 60*option)
                                    )
                                    .addBuilder(RelativeLocation(2.158019, 0.0, 6.083975),
                                        PointsBuilder()
                                            .addCircle(0.5, 10*option)
                                            .addBuilder(RelativeLocation(-4.533019, 0.0, -3.958975),
                                                PointsBuilder()
                                            )
                                    )
                                    .addLine(RelativeLocation(20.75, 0.0, -12.0), RelativeLocation(6.922524, 0.0, 0.11427), 40*option)
                                    .addLine(RelativeLocation(-20.78461, 0.0, -12.0), RelativeLocation(-6.922524, 0.0, 0.11427), 40*option)
                                    .addLine(RelativeLocation(2.0, 0.0, -1.0), RelativeLocation(7.020888, 0.0, -12.0), 30*option)
                                    .addLine(RelativeLocation(-2.0, 0.0, -1.0), RelativeLocation(-7.020888, 0.0, -12.0), 30*option)
                                    .addBuilder(RelativeLocation(0.0, 0.0, -6.0),
                                        PointsBuilder()
                                            .addCircle(4.0, 60*option)
                                            .addBuilder(RelativeLocation(0.0, 0.0, 19.125),
                                                PointsBuilder()
                                                    .addCircle(4.0, 60*option)
                                            )
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.221219, 1.307988, 0.0), RelativeLocation(-5.722348, 0.331338, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon, 0.015915*PI)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 15.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(6.0, 60*option)
                                    .addCircle(2.0, 25*option)
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(5, 7.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .addBezierCurve(RelativeLocation(1.0, 0.0, 5.0), RelativeLocation(-1.0, 0.0, 5.0), RelativeLocation(-1.0, 0.0, -5.0), RelativeLocation(1.0, 0.0, -5.0), 30*option)
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                p.rotateTo(-it)
                                                res.addAll(p
                                                    .pointsOnEach { rel -> rel.add(it) }
                                                    .createWithoutClone()
                                                )
                                            }
                                        res
                                    }
                                    .addBezierCurve(RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, 0.0), RelativeLocation(-1.0, 0.0, -1.0), RelativeLocation(1.0, 0.0, -1.0), 20*option)
                                    .addBezierCurve(RelativeLocation(0.0, 0.0, 0.0), RelativeLocation(-2.0, 0.0, 0.0), RelativeLocation(-1.0, 0.0, 1.0), RelativeLocation(1.0, 0.0, 1.0), 20*option)
                                    .addBuilder(RelativeLocation(1.0, 0.0, 0.5),
                                        PointsBuilder()
                                            .addCircle(0.5, 10*option)
                                            .addBuilder(RelativeLocation(-2.0, 0.0, -1.0),
                                                PointsBuilder()
                                                    .addCircle(0.5, 10*option)
                                            )
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.097065, 1.265818, 0.0), RelativeLocation(-5.936795, 0.319131, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon, -PI/64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 25.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addWith {
                                                val res = arrayListOf<RelativeLocation>()
                                                getPolygonInCircleVertices(3, 8.0)
                                                    .forEach { it ->
                                                        val p = PointsBuilder()
                                                            .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                            .addFillTriangle(RelativeLocation(0.0, 0.0, 2.0), RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(-2.0, 0.0, 0.0), 1.5*option)
                                                        p.rotateTo(it)
                                                        res.addAll(p
                                                            .pointsOnEach { rel -> rel.add(it) }
                                                            .createWithoutClone()
                                                        )
                                                    }
                                                res
                                            }
                                            .rotateAsAxis(0.333333*PI, RelativeLocation(0.0, 1.0, 0.0))
                                            .addPolygonInCircle(3, 30*option, 9.0)
                                            .addPolygonInCircle(3, 30*option, 11.0)
                                    )
                                    .addPolygonInCircle(6, 30*option, 13.0)
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(6, 30*option, 13.0)
                                            .rotateAsAxis(-0.166667*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.097065, 1.265818, 0.0), RelativeLocation(-5.936795, 0.319131, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon, PI/128)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 35.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(5, 50*option, 30.0)
                                            .rotateAsAxis(0.2*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                                    .addPolygonInCircle(5, 50*option, 30.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.097065, 1.265818, 0.0), RelativeLocation(-5.936795, 0.319131, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon, -PI/32)
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
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(12.0, 120*option)
                                    .addWith {
                                        val res = arrayListOf<RelativeLocation>()
                                        getPolygonInCircleVertices(4, 3.0)
                                            .forEach { it ->
                                                val p = PointsBuilder()
                                                    .addBezierCurve(RelativeLocation(0.0, 0.0, 7.0), RelativeLocation(1.0, 0.0, 6.0), RelativeLocation(1.0, 0.0, 0.0), RelativeLocation(-1.0, 0.0, 0.0), 20*option)
                                                    .addBezierCurve(RelativeLocation(0.0, 0.0, 7.0), RelativeLocation(-1.0, 0.0, 6.0), RelativeLocation(-1.0, 0.0, 0.0), RelativeLocation(1.0, 0.0, 0.0), 20*option)
                                                    .addLine(RelativeLocation(1.0, 0.0, 7.0), RelativeLocation(-1.0, 0.0, 7.0), 10*option)
                                                    .addLine(RelativeLocation(0.0, 0.0, 6.75), RelativeLocation(0.0, 0.0, 3.75), 10*option)
                                                    .axis(RelativeLocation(0.0, 0.0, 1.0))
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
                                                getPolygonInCircleVertices(4, 8.0)
                                                    .forEach { it ->
                                                        val p = PointsBuilder()
                                                            .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                                                PointsBuilder()
                                                                    .addBezierCurve(RelativeLocation(-2.0, 0.0, 1.0), RelativeLocation(-1.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(-2.0, 0.0, -1.0), 18*option)
                                                                    .addBezierCurve(RelativeLocation(-2.0, 0.0, -1.0), RelativeLocation(-1.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(-2.0, 0.0, 1.0), 18*option)
                                                                    .addBezierCurve(RelativeLocation(-3.0, 0.0, 1.0), RelativeLocation(0.0, 0.0, 0.0), RelativeLocation(2.0, 0.0, 1.0), RelativeLocation(-2.0, 0.0, 0.0), 18*option)
                                                                    .addBezierCurve(RelativeLocation(0.125, 0.0, 1.0), RelativeLocation(-2.875, 0.0, 0.0), RelativeLocation(-2.0, 0.0, 1.0), RelativeLocation(2.0, 0.0, 0.0), 18*option)
                                                                    .addBezierCurve(RelativeLocation(-3.0, 0.0, -1.0), RelativeLocation(0.0, 0.0, 0.0), RelativeLocation(2.0, 0.0, -1.0), RelativeLocation(-2.0, 0.0, 0.0), 18*option)
                                                                    .addBezierCurve(RelativeLocation(0.125, 0.0, -1.0), RelativeLocation(-2.875, 0.0, 0.0), RelativeLocation(-2.0, 0.0, -1.0), RelativeLocation(2.0, 0.0, 0.0), 18*option)
                                                                    .rotateAsAxis(0.5*PI, RelativeLocation(0.0, 1.0, 0.0))
                                                            )
                                                            .axis(RelativeLocation(0.0, 0.0, 1.0))
                                                        p.rotateTo(it)
                                                        res.addAll(p
                                                            .pointsOnEach { rel -> rel.add(it) }
                                                            .createWithoutClone()
                                                        )
                                                    }
                                                res
                                            }
                                            .rotateAsAxis(-0.25*PI, RelativeLocation(0.0, 1.0, 0.0))
                                    )
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEndRodEffect(UUID.randomUUID())
                                        size = 0.3F
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.097065, 1.265818, 0.0), RelativeLocation(-5.936795, 0.319131, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon,PI/64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, -15.0, 0.0)

        result[
            CompositionData().apply { order = orderCounter++ }
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(40.0, 240, 8.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                        age = Random.nextInt(maxAge)
                                        size = 1.0F
                                        color = this@StarryMagicComposition.color
                                    }
                            }
                            loadScaleHelperBezierValue(0.01, 1.0, 10, RelativeLocation(4.097065, 1.265818, 0.0), RelativeLocation(-5.936795, 0.319131, 0.0))
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(directon, 0.015915*PI)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 10.0, 0.0)

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
            if (status.isDisable()) {
                playCParticleAlphaTransition(20f, CParticleCurve.linear(1f, 0f))
            }
            age++
            if (!client && syncedAnimationIndex != animate.animationIndex) {
                syncedAnimationIndex = animate.animationIndex
                markDirty()
            }
            rotateToPoint(directon)
            if (age > 70){
                scaleHelper.doScale()
            }
        }
    }
}
