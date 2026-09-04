package cn.coostack.usefulmagic.particles.composition.formation

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.MathPresets
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * 显示中型阵法的多级嵌套 GPU 粒子图案。
 *
 * 示例：设置 [formationPos] 后在中型阵法核心下方生成。
 * 禁止作为未绑定阵法核心的独立装饰粒子使用。
 */
@CooAutoRegister
class MidFormationComposition(
    position: Vec3 = Vec3.ZERO,
    world: Level? = null
) : FormationComposition(position, world) {
    @CodecField
    override var formationPos: BlockPos = BlockPos.ZERO

    @CodecField
    override var formationStatusName: String = FormationStatus.IDLE.name

    @CodecField
    override var time: Int = 0

    /** 中型阵法只由各嵌套形状维护自己的旋转动画。 */
    override fun displayParticleAnimate() = Unit

    /** @return 中型阵法两组嵌套图形的粒子数据 */
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val upperRing = CompositionData().setDisplayerSupplier { uuid ->
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(uuid)
                    .applyBuilder(
                        PointsBuilder()
                            .addWith {
                                val vertices = Math3DUtil.getPolygonInCircleVertices(12, 29.0)
                                val points = ArrayList<RelativeLocation>()
                                vertices.forEachIndexed { index, origin ->
                                    points.addAll(
                                        PointsBuilder()
                                            .addWith {
                                                Math3DUtil.rotatePointsToPoint(
                                                    MathPresets.withRomaNumber(index + 1, 3.0),
                                                    origin,
                                                    RelativeLocation.yAxis()
                                                )
                                            }
                                            .rotateAsAxis(PI, origin)
                                            .create()
                                            .onEach { it.add(origin) }
                                    )
                                }
                                points
                            }
                            .addCircle(31.0, 720)
                            .addCircle(27.0, 640)
                    ) {
                        endRodData()
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .setReversedScaleOnCompositionStatus(this@MidFormationComposition)
                    .applyDisplayAction {
                        addPreTickAction {
                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                rotateAsAxis(PI / 256)
                            } else {
                                rotateAsAxis(PI / 512)
                            }
                        }
                    }
            )
        }

        val mainRing = CompositionData().setDisplayerSupplier { uuid ->
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(uuid)
                    .applyBuilder(
                        PointsBuilder()
                            .addCircle(32.0, 720)
                            .addBuilder(
                                RelativeLocation(-20.0, 0.0, 0.0),
                                PointsBuilder().addCircle(12.0, 480)
                            )
                    ) {
                        endRodData()
                    }
                    .applyPoint(RelativeLocation(12.0, 0.0, 0.0)) {
                        CompositionData().setDisplayerSupplier { childUuid ->
                            ParticleDisplayer.withComposition(
                                ParticleShapeComposition(childUuid)
                                    .applyBuilder(
                                        PointsBuilder()
                                            .addCircle(20.0, 720)
                                            .rotateAsAxis(PI / 3)
                                            .addPolygonInCircle(6, 100, 20.0)
                                            .rotateAsAxis(PI / 6)
                                            .addPolygonInCircle(6, 100, 20.0)
                                    ) {
                                        endRodData()
                                    }
                                    .loadScaleHelper(0.01, 1.0, 10)
                                    .setReversedScaleOnCompositionStatus(this@MidFormationComposition)
                                    .applyDisplayAction {
                                        addPreTickAction {
                                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                rotateAsAxis(PI / 64)
                                            } else {
                                                rotateAsAxis(PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    .applyPoint(RelativeLocation(12.0, 0.0, 0.0)) {
                        CompositionData().setDisplayerSupplier { childUuid ->
                            ParticleDisplayer.withComposition(
                                ParticleShapeComposition(childUuid)
                                    .applyPoint(RelativeLocation(3.0, 0.0, 0.0)) {
                                        CompositionData().setDisplayerSupplier { innerUuid ->
                                            ParticleDisplayer.withComposition(
                                                ParticleShapeComposition(innerUuid)
                                                    .applyBuilder(
                                                        PointsBuilder()
                                                            .addPolygonInCircle(3, 120, 10.0)
                                                            .rotateAsAxis(PI / 3)
                                                            .addPolygonInCircle(3, 120, 10.0)
                                                            .addCircle(10.0, 360)
                                                            .addWith {
                                                                val builder = PointsBuilder()
                                                                PointsBuilder()
                                                                    .addPolygonInCircleVertices(3, 10.0)
                                                                    .create()
                                                                    .forEach { point ->
                                                                        builder.addBuilder(
                                                                            point,
                                                                            PointsBuilder()
                                                                                .addCircle(5.0, 240)
                                                                                .addPolygonInCircle(4, 50, 5.0)
                                                                                .addPolygonInCircle(4, 50, 4.0)
                                                                                .rotateAsAxis(PI / 4)
                                                                                .addPolygonInCircle(4, 50, 5.0)
                                                                                .addPolygonInCircle(4, 50, 4.0)
                                                                        )
                                                                    }
                                                                builder.create()
                                                            }
                                                            .addPolygonInCircle(6, 120, 5.0)
                                                            .rotateAsAxis(PI / 6)
                                                            .addPolygonInCircle(6, 120, 5.0)
                                                    ) {
                                                        endRodData()
                                                    }
                                                    .loadScaleHelper(0.01, 1.0, 20)
                                                    .setReversedScaleOnCompositionStatus(
                                                        this@MidFormationComposition
                                                    )
                                                    .applyDisplayAction {
                                                        addPreTickAction {
                                                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                                rotateAsAxis(PI / 64)
                                                            } else {
                                                                rotateAsAxis(PI / 128)
                                                            }
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                    .loadScaleHelper(0.01, 1.0, 20)
                                    .setReversedScaleOnCompositionStatus(this@MidFormationComposition)
                                    .applyDisplayAction {
                                        addPreTickAction {
                                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                rotateAsAxis(-PI / 64)
                                            } else {
                                                rotateAsAxis(-PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    .applyPoint(RelativeLocation(-20.0, 0.0, 0.0)) {
                        CompositionData().setDisplayerSupplier { childUuid ->
                            ParticleDisplayer.withComposition(
                                ParticleShapeComposition(childUuid)
                                    .applyBuilder(
                                        PointsBuilder()
                                            .addCircle(11.0, 360)
                                            .addCircle(8.0, 360)
                                            .addWith {
                                                val builder = PointsBuilder()
                                                PointsBuilder()
                                                    .addPolygonInCircleVertices(12, 10.0)
                                                    .create()
                                                    .forEachIndexed { index, point ->
                                                        builder.addBuilder(
                                                            point,
                                                            PointsBuilder().addPoints(
                                                                Math3DUtil.rotatePointsToPoint(
                                                                    MathPresets.withRomaNumber(index + 1, 2.0),
                                                                    point,
                                                                    RelativeLocation.yAxis()
                                                                )
                                                            ).rotateAsAxis(PI, point)
                                                        )
                                                    }
                                                builder.create()
                                            }
                                            .addCircle(7.0, 240)
                                            .addFourierSeries(
                                                FourierSeriesBuilder()
                                                    .count(320 * ParticleOption.getParticleCounts())
                                                    .addFourier(5.0, 5.0)
                                                    .addFourier(5.0, -2.0)
                                                    .scale(0.7)
                                            )
                                            .addBuilder(
                                                RelativeLocation(0.0, 1.5, 0.0),
                                                PointsBuilder()
                                                    .addDiscreteCircleXZ(5.0, 240, 0.4)
                                                    .addPolygonInCircle(4, 120, 5.0)
                                                    .rotateAsAxis(PI / 4)
                                                    .addPolygonInCircle(4, 120, 5.0)
                                            )
                                    ) {
                                        endRodData()
                                    }
                                    .loadScaleHelper(0.01, 1.0, 20)
                                    .setReversedScaleOnCompositionStatus(this@MidFormationComposition)
                                    .applyDisplayAction {
                                        addPreTickAction {
                                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                rotateAsAxis(PI / 64)
                                            } else {
                                                rotateAsAxis(PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .setReversedScaleOnCompositionStatus(this@MidFormationComposition)
                    .applyDisplayAction {
                        addPreTickAction {
                            if (this@MidFormationComposition.formationStatus == FormationStatus.WORKING) {
                                rotateAsAxis(-PI / 128)
                            } else {
                                rotateAsAxis(-PI / 512)
                            }
                        }
                    }
            )
        }

        return mutableMapOf(
            mainRing to RelativeLocation(0.0, 0.01, 0.0),
            upperRing to RelativeLocation(0.0, 0.5, 0.0)
        )
    }

    /** @return 保留浅蓝 end rod 外观的 GPU 叶子粒子数据 */
    private fun endRodData(): CompositionData {
        return CompositionData()
            .setDisplayerSupplier { uuid ->
                ParticleDisplayer.withCParticle(uuid)
            }
            .addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
                color = Math3DUtil.colorOf(100, 200, 255)
            }
    }
}
