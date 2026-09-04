package cn.coostack.usefulmagic.particles.composition.entity

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.network.particle.composition.SequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.SortedMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI
import kotlin.random.Random

/**
 * level 3
 */
@CooAutoRegister
class CraftingLevel3Composition(
    position: Vec3, world: Level? = null
) : AutoSequencedParticleComposition(position, world) {

    val options: Int
        get() = ParticleOption.getParticleCounts()

    @CodecField
    var age = 0

    init {
        animate.addAnimate(1) {
            age > 1
        }.addAnimate(1) {
            age > 6
        }.addAnimate(1) {
            age > 11
        }.addAnimate(1) {
            age > 16
        }
        status.closedInternal = 20
    }

    override fun onDisplay() {
        var syncedAnimationIndex = animate.animationIndex
        addPreTickAction {
            age++
            if (!client && syncedAnimationIndex != animate.animationIndex) {
                syncedAnimationIndex = animate.animationIndex
                markDirty()
            }
            rotateAsAxis(PI / 64)
        }
    }

    fun ParticleComposition.doWithAlpha(alphaTick: Int = 10) {
        var reverse = false
        this.addPreTickAction {
            if (!reverse) {
                playCParticleAlphaTransition(alphaTick.toFloat(), CParticleCurve.linear(0f, 1f))
            } else {
                playCParticleAlphaTransition(alphaTick.toFloat(), CParticleCurve.linear(1f, 0f))
            }
            if (status.displayStatus == 2) {
                reverse = true
            }
        }
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val res = sortedMapOf<CompositionData, RelativeLocation>()
        val foot = RelativeLocation(0.0, 0.1, 0.0)
        val random = Random(System.currentTimeMillis())
        var order = 0
        fun single(): CompositionData = CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
        }.addCParticleInstanceInit {
            effect = ControlableEndRodEffect(UUID.randomUUID())
            color = Math3DUtil.colorOf(210, random.nextInt(100, 140), 255)
        }
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(2.0, 30 * options)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .applyDisplayAction {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateAsAxis(PI / 32)
                            }
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = foot

        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(4.0, 45 * options)
                                .addCircle(5.0, 45 * options)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .applyDisplayAction {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateAsAxis(PI / 32)
                            }
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = foot

        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(4, 20 * options, 7.0)
                                .addPolygonInCircle(4, 20 * options, 8.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 20 * options, 7.0)
                                .addPolygonInCircle(4, 20 * options, 8.0)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .applyDisplayAction {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateAsAxis(-PI / 32)
                            }
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = foot

        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    getPolygonInCircleVertices(6, 8.0).forEach { origin ->
                                        res.addAll(
                                            PointsBuilder()
                                                .addDiscreteCircleXZ(2.0, 30 * options, 0.1)
                                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                                .rotateAsAxis(PI / 3)
                                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                                .pointsOnEach { it -> it.add(origin) }
                                                .create()
                                        )
                                    }
                                    res
                                }.addCircle(8.0, 120 * options)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .applyDisplayAction {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateAsAxis(PI / 64)
                            }
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = foot
        return res
    }

}
