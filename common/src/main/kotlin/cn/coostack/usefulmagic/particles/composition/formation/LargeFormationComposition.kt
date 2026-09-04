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
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * 显示大型阵法的多级嵌套 GPU 粒子图案。
 *
 * 示例：设置 [formationPos] 后在大型阵法核心位置生成。
 * 禁止作为未绑定阵法核心的独立装饰粒子使用。
 */
@CooAutoRegister
class LargeFormationComposition(
    position: Vec3 = Vec3.ZERO,
    world: Level? = null
) : FormationComposition(position, world) {
    @CodecField
    override var formationPos: BlockPos = BlockPos.ZERO

    @CodecField
    override var formationStatusName: String = FormationStatus.IDLE.name

    @CodecField
    override var time: Int = 0

    /** 控制整个大型阵法的出现与收束缩放。 */
    private val scaleHelper = CompositionScaleHelper(0.01, 1.0, 20).apply {
        loadControler(this@LargeFormationComposition)
    }

    override fun displayParticleAnimate() {
        if (status.isDisable()) {
            reverseScaleOrRemove(scaleHelper)
        } else {
            scaleHelper.doScale()
        }
    }

    override fun onDisplay() {
        scaleHelper.doScaleTo(time)
        super.onDisplay()
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val vertices = PointsBuilder().addPolygonInCircleVertices(3, 37.0).create()
        val first = CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                getShapeComposition(it)
                    .applyPoint(RelativeLocation()) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withComposition(
                                getShapeComposition(it)
                                    .applyPoint(RelativeLocation()) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder()
                                                        .addCircle(64.0, 480 * ParticleOption.getParticleCounts())
                                                        .addDottedCircle(
                                                            48.0,
                                                            480 * ParticleOption.getParticleCounts(),
                                                            16,
                                                            PI / 16
                                                        )
                                                        .addCircle(10.0, 270 * ParticleOption.getParticleCounts())
                                                        .addPolygonInCircle(
                                                            3,
                                                            120 * ParticleOption.getParticleCounts(),
                                                            64.0
                                                        )
                                                        .addPolygonInCircle(
                                                            3,
                                                            40 * ParticleOption.getParticleCounts(),
                                                            10.0
                                                        )
                                                        .addPolygonInCircle(
                                                            3,
                                                            40 * ParticleOption.getParticleCounts(),
                                                            8.0
                                                        )
                                                        .rotateAsAxis(PI / 3)
                                                        .addPolygonInCircle(
                                                            3,
                                                            40 * ParticleOption.getParticleCounts(),
                                                            10.0
                                                        )
                                                        .addPolygonInCircle(
                                                            3,
                                                            40 * ParticleOption.getParticleCounts(),
                                                            8.0
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                            rotateAsAxis(PI / 128)
                                                        } else {
                                                            rotateAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }.applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder()
                                                        .addDottedCircle(
                                                            36.0,
                                                            480 * ParticleOption.getParticleCounts(),
                                                            16,
                                                            PI / 16
                                                        )
                                                        .rotateAsAxis(PI / 16)
                                                        .addDottedCircle(
                                                            60.0,
                                                            480 * ParticleOption.getParticleCounts(),
                                                            16,
                                                            PI / 16
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                            rotateAsAxis(-PI / 128)
                                                        } else {
                                                            rotateAsAxis(-PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .applyPoint(
                        vertices[0].clone()
                    ) {
                        CompositionData().setDisplayerSupplier {
                            ParticleDisplayer.withComposition(
                                getShapeComposition(it)
                                    .applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder().addCircle(
                                                        27.0,
                                                        480 * ParticleOption.getParticleCounts()
                                                    )
                                                        .addWith {
                                                            val builder = PointsBuilder()
                                                            PointsBuilder()
                                                                .addPolygonInCircleVertices(12, 23.0)
                                                                .create().forEachIndexed { index, it ->
                                                                    builder.addBuilder(
                                                                        it, PointsBuilder().addPoints(
                                                                            Math3DUtil.rotatePointsToPoint(
                                                                                MathPresets.withRomaNumber(
                                                                                    index + 1,
                                                                                    4.0
                                                                                ),
                                                                                it,
                                                                                RelativeLocation.yAxis()
                                                                            )
                                                                        ).rotateAsAxis(PI, it)
                                                                    )
                                                                }
                                                            builder.create()
                                                        }
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        rotateAsAxis(PI / 180)
                                                    }
                                                }
                                            )
                                        }
                                    }.applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder()
                                                        .addLine(
                                                            RelativeLocation(),
                                                            RelativeLocation(-20.0, 0.0, 0.0),
                                                            30 * ParticleOption.getParticleCounts()
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        rotateAsAxis(PI / 30)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    .applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder()
                                                        .addLine(
                                                            RelativeLocation(),
                                                            RelativeLocation(-10.0, 0.0, 0.0),
                                                            15 * ParticleOption.getParticleCounts()
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        rotateAsAxis(PI / 180)
                                                    }
                                                }
                                            )
                                        }
                                    }.applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier { it ->
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder()
                                                        .addDottedCircle(
                                                            16.0, 170 * ParticleOption.getParticleCounts(), 8, PI / 8
                                                        )
                                                        .rotateAsAxis(PI / 8)
                                                        .addDottedCircle(
                                                            11.0, 480 * ParticleOption.getParticleCounts(), 8, PI / 8
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        rotateAsAxis(-PI / 90)
                                                    }
                                                }
                                            )
                                        }
                                    }.applyPoint(
                                        RelativeLocation(0.0, 0.5, 0.0)
                                    ) {
                                        CompositionData().setDisplayerSupplier { it ->
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it)
                                                    .applyBuilder(
                                                        PointsBuilder()
                                                            .addDottedCircle(
                                                                13.0,
                                                                180 * ParticleOption.getParticleCounts(),
                                                                8,
                                                                PI / 8
                                                            )
                                                            .addDottedCircle(
                                                                19.0,
                                                                180 * ParticleOption.getParticleCounts(),
                                                                8,
                                                                PI / 8
                                                            )
                                                    ) {
                                                        getSingleCompositionData()
                                                    }.applyDisplayAction {
                                                        this.addPreTickAction {
                                                            rotateAsAxis(PI / 90)
                                                        }
                                                    }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .applyPoint(
                        vertices[1].clone()
                    ) {
                        CompositionData().setDisplayerSupplier { it ->
                            ParticleDisplayer.withComposition(
                                getShapeComposition(it).applyPoint(
                                    RelativeLocation()
                                ) {
                                    CompositionData().setDisplayerSupplier {
                                        ParticleDisplayer.withComposition(
                                            getShapeComposition(it)
                                                .applyBuilder(
                                                    PointsBuilder()
                                                        .addCircle(27.0, 480 * ParticleOption.getParticleCounts())
                                                        .addPolygonInCircle(
                                                            5,
                                                            120 * ParticleOption.getParticleCounts(),
                                                            27.0
                                                        )
                                                        .rotateAsAxis(PI / 5)
                                                        .addPolygonInCircle(
                                                            5,
                                                            120 * ParticleOption.getParticleCounts(),
                                                            27.0
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                            rotateAsAxis(PI / 128)
                                                        } else {
                                                            rotateAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }.applyPoint(RelativeLocation()) {
                                    CompositionData().setDisplayerSupplier {
                                        ParticleDisplayer.withComposition(
                                            getShapeComposition(it).applyBuilder(
                                                PointsBuilder().addFourierSeries(
                                                    FourierPresets.circlesAndTriangles().scale(2.0)
                                                        .count(600 * ParticleOption.getParticleCounts())
                                                )
                                            ) {
                                                getSingleCompositionData()
                                            }.applyDisplayAction {
                                                addPreTickAction {
                                                    if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                        rotateAsAxis(-PI / 128)
                                                    } else {
                                                        rotateAsAxis(-PI / 256)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                    .applyPoint(
                        vertices[2].clone()
                    ) {
                        CompositionData().setDisplayerSupplier { it ->
                            ParticleDisplayer.withComposition(
                                getShapeComposition(it)
                                    .applyPoint(RelativeLocation()) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder().addCircle(
                                                        27.0,
                                                        480 * ParticleOption.getParticleCounts()
                                                    )
                                                        .addFourierSeries(
                                                            FourierPresets
                                                                .rhombic()
                                                                .scale(27.0 / 4.0)
                                                                .count(480 * ParticleOption.getParticleCounts())
                                                        )
                                                        .rotateAsAxis(PI / 4)
                                                        .addFourierSeries(
                                                            FourierPresets
                                                                .rhombic()
                                                                .scale(27.0 / 4.0)
                                                                .count(480 * ParticleOption.getParticleCounts())
                                                        )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                            rotateAsAxis(PI / 128)
                                                        } else {
                                                            rotateAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }.applyPoint(
                                        RelativeLocation()
                                    ) {
                                        CompositionData().setDisplayerSupplier {
                                            ParticleDisplayer.withComposition(
                                                getShapeComposition(it).applyBuilder(
                                                    PointsBuilder().addFourierSeries(
                                                        FourierPresets.bowsOnAllSides()
                                                            .count(
                                                                480 * ParticleOption.getParticleCounts()
                                                            )
                                                    )
                                                ) {
                                                    getSingleCompositionData()
                                                }.applyDisplayAction {
                                                    this.addPreTickAction {
                                                        if (this@LargeFormationComposition.formationStatus == FormationStatus.WORKING) {
                                                            rotateAsAxis(-PI / 128)
                                                        } else {
                                                            rotateAsAxis(-PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .applyDisplayAction {
                        this.addPreTickAction {
                            rotateAsAxis(-PI / 256)
                        }
                    }
            )
        }
        val res = HashMap<CompositionData, RelativeLocation>()
        res[first] = RelativeLocation()
        return res
    }

    /**
     * 创建随父组合禁用状态反向缩放的子形状。
     *
     * @param uuid 子组合控制标识
     * @return 已配置缩放生命周期的形状组合
     */
    private fun getShapeComposition(uuid: UUID): ParticleShapeComposition {
        return ParticleShapeComposition(uuid)
            .loadScaleHelper(0.01, 1.0, 20)
            .setReversedScaleOnCompositionStatus(this)
    }

    /** @return 保留青色 end rod 外观的 GPU 叶子粒子数据 */
    private fun getSingleCompositionData(): CompositionData {
        return CompositionData()
            .setDisplayerSupplier { uuid ->
                ParticleDisplayer.withCParticle(uuid)
            }
            .addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
                color = Math3DUtil.colorOf(0, 255, 255)
            }
    }
}
