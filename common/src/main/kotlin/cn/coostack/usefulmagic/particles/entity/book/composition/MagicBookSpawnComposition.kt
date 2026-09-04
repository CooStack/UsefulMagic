package cn.coostack.usefulmagic.particles.entity.book.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.PI

@CooAutoRegister
class MagicBookSpawnComposition(position: Vec3, world: Level? = null) :
    AutoSequencedParticleComposition(position, world) {


    @CodecField
    var age = 0

    init {
        status.closedInternal = 60
        animate.addAnimate(1) {
            age > 1
        }.addAnimate(1) {
            age > 5
        }.addAnimate(1) {
            age > 10
        }
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()


    override fun remove() {
        if (status.displayStatus == 2) {
            super.remove()
        } else {
            status.setStatus(2)
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
        }
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val res = TreeMap<CompositionData, RelativeLocation>()
        var order = 0
        fun single(): CompositionData {
            return CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withCParticle(it)
            }.addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
            }
        }
        // 第一层 小圆+ 六芒星
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(2.0, 60 * options)
                                .addCycloidGraphic(1.0, 2.0, 2, -1, 120 * options, 2 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(1.0, 2.0, 2, -1, 120 * options, 2 / 3.0)
                        ) {
                            single().addCParticleInstanceInit {
                                color = Math3DUtil.colorOf(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .applyDisplayAction {
                            addPreTickAction {
                                rotateAsAxis(PI / 64)
                            }
                            setReversedScaleOnCompositionStatus(this@MagicBookSpawnComposition)
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 几何图形符文
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(6.0, 80 * options)
                                .addPolygonInCircle(6, 20 * options, 4.0)
                                .addWith {
                                    val res = arrayListOf<RelativeLocation>()
                                    PointsBuilder()
                                        .addPolygonInCircleVertices(6, 4.0)
                                        .create()
                                        .forEachIndexed { index, origin ->
                                            res.addAll(
                                                PointsBuilder()
                                                    .addPolygonInCircle(index + 3, 20 * options, 2.0)
                                                    .rotateAsAxis(PI / (index + 1))
                                                    .pointsOnEach { it -> it.add(origin) }
                                                    .create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single().addCParticleInstanceInit {
                                color = Math3DUtil.colorOf(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20).applyDisplayAction {
                            addPreTickAction {
                                rotateAsAxis(-PI / 48)
                            }
                            setReversedScaleOnCompositionStatus(this@MagicBookSpawnComposition)
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 大八芒星+大圆
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(10.0, 80 * options)
                                .addPolygonInCircle(4, 30 * options, 10.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 30 * options, 10.0)
                                .addWith {
                                    val res = arrayListOf<RelativeLocation>()
                                    PointsBuilder()
                                        .addPolygonInCircleVertices(4, 10.0)
                                        .create()
                                        .forEach { origin ->
                                            res.addAll(
                                                PointsBuilder()
                                                    .addCircle(2.0, 30 * options)
                                                    .addPolygonInCircle(3, 8 * options, 2.0)
                                                    .rotateAsAxis(PI / 3)
                                                    .addPolygonInCircle(3, 8 * options, 2.0)
                                                    .pointsOnEach { it ->
                                                        it.add(origin)
                                                    }
                                                    .create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single().addCParticleInstanceInit {
                                color = Math3DUtil.colorOf(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20).applyDisplayAction {
                            addPreTickAction {
                                rotateAsAxis(PI / 32)
                            }
                            setReversedScaleOnCompositionStatus(this@MagicBookSpawnComposition)
                        }
                )
            }.apply {
                this.order = order++
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        return res
    }
}
