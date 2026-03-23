package cn.coostack.usefulmagic.particles.composition.explosion

import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.particles.ControlableParticleEffect
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.Vec3i
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

@CooAutoRegister
class ExplosionMagicComposition(
    position: Vec3,
    world: Level?
) : AutoSequencedParticleComposition(position, world) {

    val random = Random(System.currentTimeMillis())

    @CodecField
    var age = 0

    @CodecField
    var rotateDirection = RelativeLocation.xAxis()

    @CodecField
    var maxAge = 240
    val alphaHelper = CompositionAlphaHelper(
        0.01, 1.0, 20,
    )

    init {
        setDisabledInterval(30)
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
    }

    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }

    override fun beforeDisplay(map: Map<CompositionData, RelativeLocation>) {
        Math3DUtil.rotatePointsToPoint(
            map.values.toList(), rotateDirection, axis
        )
        axis = rotateDirection
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val res = TreeMap<CompositionData, RelativeLocation>()
        var order = 0

        val handler: ParticleShapeComposition.() -> ParticleShapeComposition =
            {
                this.loadScaleHelperBezierValue(
                    0.01, 1.0, 20,
                    RelativeLocation(1.0, 0.99, 0.0),
                    RelativeLocation(-19.0, 0.0, 0.0),
                ).applyBeforeDisplayAction {
                    preRotateTo(it, rotateDirection)
                }
                    .applyDisplayAction {
                        this.addPreTickAction {
                            rotateAsAxis(PI / 32)
                        }
                        setReversedScaleOnCompositionStatus(this@ExplosionMagicComposition)
                    }
            }
        var y = -18.01
        val added = 6.0
        res[genSingleComposition(15.0, order++, handler)] = RelativeLocation(0.0, y, 0.0)
        res[genSingleComposition(20.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(13.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(7.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(5.5, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(2.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(3.5, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(10.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(7.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(25.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(10.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(20.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        res[genSingleComposition(13.0, order++, handler)] = RelativeLocation(0.0, let { y += added; y }, 0.0)
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            if (age++ > maxAge) {
                status.setStatus(2)
            }
            if (status.displayStatus == 2) {
                alphaHelper.decreaseAlpha()
                toggleRelative()
            }
            if ((age - 1) % 5 == 0 && !client) {
                addSingle()
            }
        }
    }

    val option: Int
        get() = ParticleOption.getParticleCounts()

    /**
     * @param r 大小
     */
    private fun genSingleComposition(
        r: Double,
        order: Int,
        styleHandler: ParticleShapeComposition.() -> ParticleShapeComposition
    ): CompositionData {
        require(r > 0)
        val subCircleRadius = r / 4
        val halfCircleRadius = r / 2
        val circleCount = (5 * option * r).toInt().coerceAtLeast(4)
        val subCircleCount = circleCount / 4
        val halfCircleCount = circleCount / 2
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                styleHandler(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    var currentTheta = 0.0
                                    repeat(8) {
                                        currentTheta += PI / 4
                                        res.addAll(
                                            PointsBuilder()
                                                .addBezierCurve(
                                                    RelativeLocation(halfCircleRadius * 2, 0.0, 0.0),
                                                    RelativeLocation(
                                                        halfCircleRadius * 0.05,
                                                        halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    RelativeLocation(
                                                        -halfCircleRadius * 0.05,
                                                        halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    halfCircleCount
                                                )
                                                .rotateAsAxis(PI / 2, RelativeLocation.xAxis())
                                                .rotateAsAxis(currentTheta)
                                                .create()
                                        )
                                    }
                                    currentTheta = PI / 24
                                    repeat(8) {
                                        currentTheta += PI / 4
                                        res.addAll(
                                            PointsBuilder()
                                                .addBezierCurve(
                                                    RelativeLocation(halfCircleRadius * 2, 0.0, 0.0),
                                                    RelativeLocation(
                                                        halfCircleRadius * 0.05,
                                                        -halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    RelativeLocation(
                                                        -halfCircleRadius * 0.05,
                                                        -halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    halfCircleCount
                                                )
                                                .rotateAsAxis(PI / 2, RelativeLocation.xAxis())
                                                .rotateAsAxis(currentTheta)
                                                .create()
                                        )
                                    }
                                    res
                                }
                        ) {
                            genSingle(
                                ControlableEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 80, 80
                                ), 0.1f
                            )
                        }
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(subCircleRadius, subCircleCount)

                        ) {
                            genSingle(
                                ControlableEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 200, 200
                                ), 0.3f
                            )
                        }
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(r, circleCount)
                        ) {
                            genSingle(
                                ControlableEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 80, 80
                                ),
                                0.4f
                            )
                        }
                )
            )
        }.apply { this.order = order }

    }

    private fun genSingle(effect: ControlableParticleEffect, color: Vec3i, scale: Float = 0.2f): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withSingle(effect.clone().apply { this.controlUUID = it })
        }.addParticleInstanceInit {
            this.colorOfRGB(color.x, color.y, color.z)
            this.size = scale
            this.textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
        }
    }

}

