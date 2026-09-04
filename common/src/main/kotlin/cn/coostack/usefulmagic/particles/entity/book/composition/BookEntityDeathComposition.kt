package cn.coostack.usefulmagic.particles.entity.book.composition

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.MathPresets
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagicClient
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

@CooAutoRegister
class BookEntityDeathComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val res = HashMap<CompositionData, RelativeLocation>()
        val option = UsefulMagicClient.option
        fun single(scaleSize: Float = 0.2f): CompositionData {
            return CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withCParticle(
                    it
                )
            }.addCParticleInstanceInit {
                color = Math3DUtil.colorOf(255, 100, 100)
                size = scaleSize
            }
        }
        // 六芒星
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 20 * option, 4.0)
                                .addPolygonInCircle(3, 15 * option, 3.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 20 * option, 4.0)
                                .addPolygonInCircle(3, 15 * option, 3.0)
                        ) {
                            single()
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .applyDisplayAction {
                            this.setReversedScaleOnCompositionStatus(this@BookEntityDeathComposition)
                            this.addPreTickAction {
                                this.rotateAsAxis(PI / 32.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 圆
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(4.0, 60 * option)
                                // 3.5放roma数字
                                .addCircle(5.0, 60 * option)
                        ) {
                            single()
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .applyDisplayAction {
                            this.setReversedScaleOnCompositionStatus(this@BookEntityDeathComposition)
                            this.addPreTickAction {
                                this.rotateAsAxis(PI / 64.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    getPolygonInCircleVertices(10, 4.5)
                                        .forEachIndexed { index, it ->
                                            res.addAll(
                                                PointsBuilder().addPoints(MathPresets.withRomaNumber(index + 1, 1.0))
                                                    .rotateTo(it)
                                                    .pointsOnEach { p ->
                                                        p.add(it)
                                                    }.create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single(0.1f)
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .applyDisplayAction {
                            this.setReversedScaleOnCompositionStatus(this@BookEntityDeathComposition)
                            this.addPreTickAction {
                                this.rotateAsAxis(-PI / 32.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        return res
    }

    override fun onDisplay() {
    }
}