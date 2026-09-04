package cn.coostack.usefulmagic.particles.composition.formation

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * 显示小型阵法的三层 GPU 粒子图案。
 *
 * 示例：设置 [formationPos] 后在小型阵法核心下方生成。
 * 禁止作为未绑定阵法核心的独立装饰粒子使用。
 */
@CooAutoRegister
class SmallFormationComposition(
    position: Vec3 = Vec3.ZERO,
    world: Level? = null
) : FormationComposition(position, world) {
    @CodecField
    override var formationPos: BlockPos = BlockPos.ZERO

    @CodecField
    override var formationStatusName: String = FormationStatus.IDLE.name

    @CodecField
    override var time: Int = 0

    /** 控制整个小型阵法的出现与收束缩放。 */
    private val scaleHelper = CompositionScaleHelper(0.01, 1.0, 20).apply {
        loadControler(this@SmallFormationComposition)
    }

    /** 根据组合生命周期推进整体缩放。 */
    override fun displayParticleAnimate() {
        if (status.isDisable()) {
            reverseScaleOrRemove(scaleHelper)
        } else {
            scaleHelper.doScale()
        }
    }

    /** 使用服务端同步时间恢复初始缩放后启动共有生命周期。 */
    override fun onDisplay() {
        scaleHelper.doScaleTo(time)
        super.onDisplay()
    }

    /** @return 小型阵法三层嵌套图形的粒子数据 */
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val first = CompositionData().setDisplayerSupplier { uuid ->
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(uuid).applyBuilder(
                    PointsBuilder()
                        .addBuilder(
                            RelativeLocation(2.0, 0.0, 2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(2.0, 0.0, 0.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(2.0, 0.0, -2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(0.0, 0.0, 2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(0.0, 0.0, -2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(-2.0, 0.0, 2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(-2.0, 0.0, 0.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                        .addBuilder(
                            RelativeLocation(-2.0, 0.0, -2.0),
                            PointsBuilder().addCircle(0.5, 20)
                        )
                ) {
                    endRodData(0, 255, 255, 0.1f)
                }
            )
        }

        val second = CompositionData().setDisplayerSupplier { uuid ->
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(uuid)
                    .applyBuilder(
                        PointsBuilder()
                            .addBuilder(
                                RelativeLocation(0.0, 1.0, 0.0),
                                PointsBuilder().addCircle(2.5, 50 * ParticleOption.getParticleCounts())
                            )
                            .addCircle(16.0, 180 * ParticleOption.getParticleCounts())
                            .addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 10.0)
                            .addBuilder(
                                RelativeLocation(),
                                PointsBuilder().addPolygonInCircle(
                                    4,
                                    30 * ParticleOption.getParticleCounts(),
                                    10.0
                                ).rotateAsAxis(PI / 4, RelativeLocation.yAxis())
                            )
                            .addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 12.0)
                            .addBuilder(
                                RelativeLocation(),
                                PointsBuilder().addPolygonInCircle(
                                    4,
                                    30 * ParticleOption.getParticleCounts(),
                                    12.0
                                ).rotateAsAxis(PI / 4, RelativeLocation.yAxis())
                            )
                            .addBuilder(
                                RelativeLocation(0.0, 0.5, 0.0),
                                PointsBuilder().addCircle(14.0, 180 * ParticleOption.getParticleCounts())
                            )
                            .addFourierSeries(
                                FourierSeriesBuilder()
                                    .scale(1.0)
                                    .count(314 * ParticleOption.getParticleCounts())
                                    .addFourier(3.0, 4.0, 0.0)
                                    .addFourier(13.0, -3.0, 0.0)
                            )
                    ) {
                        endRodData(0, 255, 255)
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .setReversedScaleOnCompositionStatus(this@SmallFormationComposition)
                    .applyDisplayAction {
                        addPreTickAction {
                            if (this@SmallFormationComposition.formationStatus == FormationStatus.WORKING) {
                                rotateAsAxis(PI / 32)
                            } else {
                                rotateAsAxis(PI / 256)
                            }
                        }
                    }
            )
        }

        val third = CompositionData().setDisplayerSupplier { uuid ->
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(uuid)
                    .applyBuilder(
                        PointsBuilder()
                            .addBuilder(
                                RelativeLocation(0.0, 1.2, 0.0),
                                PointsBuilder().addCircle(3.5, 60 * ParticleOption.getParticleCounts())
                            )
                            .addBuilder(
                                RelativeLocation(0.0, 0.5, 0.0),
                                PointsBuilder().addCircle(4.5, 90 * ParticleOption.getParticleCounts())
                            )
                            .addCircle(7.0, 120 * ParticleOption.getParticleCounts())
                            .addPolygonInCircle(4, 50, 6.0)
                            .addBuilder(
                                RelativeLocation(),
                                PointsBuilder().addPolygonInCircle(4, 50, 6.0)
                                    .rotateAsAxis(PI / 4, RelativeLocation.yAxis())
                            )
                            .addPolygonInCircle(4, 50, 7.0)
                            .addBuilder(
                                RelativeLocation(),
                                PointsBuilder().addPolygonInCircle(4, 50, 7.0)
                                    .rotateAsAxis(PI / 4, RelativeLocation.yAxis())
                            )
                    ) {
                        endRodData(100, 200, 255)
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .setReversedScaleOnCompositionStatus(this@SmallFormationComposition)
                    .applyDisplayAction {
                        addPreTickAction {
                            if (this@SmallFormationComposition.formationStatus == FormationStatus.WORKING) {
                                rotateAsAxis(-PI / 16)
                            } else {
                                rotateAsAxis(-PI / 128)
                            }
                        }
                    }
            )
        }

        return hashMapOf(
            first to RelativeLocation(0.0, 0.01, 0.0),
            second to RelativeLocation(0.0, 0.01, 0.0),
            third to RelativeLocation(0.0, 0.01, 0.0)
        )
    }

    /**
     * 创建保留 end rod 外观的 GPU 粒子配置。
     *
     * @param red 红色通道，范围 `0..255`
     * @param green 绿色通道，范围 `0..255`
     * @param blue 蓝色通道，范围 `0..255`
     * @param size 可选粒子尺寸；`null` 表示使用 CParticle 默认值
     * @return 可用于形状 Composition 的叶子粒子数据
     */
    private fun endRodData(red: Int, green: Int, blue: Int, size: Float? = null): CompositionData {
        return CompositionData()
            .setDisplayerSupplier { uuid ->
                ParticleDisplayer.withCParticle(uuid)
            }
            .addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
                color = Math3DUtil.colorOf(red, green, blue)
                size?.let { this.size = it }
            }
    }
}
