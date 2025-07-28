package cn.coostack.usefulmagic.particles.style.formation

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.MathPresets
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.ImagePointBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.Particle
import net.minecraft.util.Identifier
import java.util.SortedSet
import java.util.UUID
import kotlin.math.PI

class LargeFormationStyle(uuid: UUID = UUID.randomUUID()) : FormationStyle(uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return LargeFormationStyle(uuid)
        }
    }

    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 20)
        .apply {
            loadControler(this@LargeFormationStyle)
        }

    override fun displayParticleAnimate() {
        if (statusHelper.displayStatus == StatusHelper.Status.DISABLE.id) {
            scaleHelper.doScaleReversed()
        } else {
            scaleHelper.doScale()
        }
    }

    override fun onDisplay() {
        scaleHelper.doScaleTo(time)
        super.onDisplay()
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val vertices = PointsBuilder().addPolygonInCircleVertices(3, 37.0).create()
        val first = StyleData {
            ParticleDisplayer.withStyle(
                getShapeStyle(it)
                    .appendPoint(RelativeLocation()) {
                        StyleData {
                            ParticleDisplayer.withStyle(
                                getShapeStyle(it)
                                    .appendPoint(RelativeLocation()) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
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
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        if (status == FormationStatus.WORKING) {
                                                            rotateParticlesAsAxis(PI / 128)
                                                        } else {
                                                            rotateParticlesAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }.appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
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
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        if (status == FormationStatus.WORKING) {
                                                            rotateParticlesAsAxis(-PI / 128)
                                                        } else {
                                                            rotateParticlesAsAxis(-PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .appendPoint(
                        vertices[0].clone()
                    ) {
                        StyleData {
                            ParticleDisplayer.withStyle(
                                getShapeStyle(it)
                                    .appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
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
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        rotateParticlesAsAxis(PI / 180)
                                                    }
                                                }
                                            )
                                        }
                                    }.appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
                                                    PointsBuilder()
                                                        .addLine(
                                                            RelativeLocation(),
                                                            RelativeLocation(-20.0, 0.0, 0.0),
                                                            30 * ParticleOption.getParticleCounts()
                                                        )
                                                ) {
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        rotateParticlesAsAxis(PI / 30)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    .appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
                                                    PointsBuilder()
                                                        .addLine(
                                                            RelativeLocation(),
                                                            RelativeLocation(-10.0, 0.0, 0.0),
                                                            15 * ParticleOption.getParticleCounts()
                                                        )
                                                ) {
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        rotateParticlesAsAxis(PI / 180)
                                                    }
                                                }
                                            )
                                        }
                                    }.appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData { it ->
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
                                                    PointsBuilder()
                                                        .addDottedCircle(
                                                            16.0, 170 * ParticleOption.getParticleCounts(), 8, PI / 8
                                                        )
                                                        .rotateAsAxis(PI / 8)
                                                        .addDottedCircle(
                                                            11.0, 480 * ParticleOption.getParticleCounts(), 8, PI / 8
                                                        )
                                                ) {
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        rotateParticlesAsAxis(-PI / 90)
                                                    }
                                                }
                                            )
                                        }
                                    }.appendPoint(
                                        RelativeLocation(0.0, 0.5, 0.0)
                                    ) {
                                        StyleData { it ->
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it)
                                                    .appendBuilder(
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
                                                        getSingleStyleDataBuilder().build()
                                                    }.toggleOnDisplay {
                                                        this.addPreTickAction {
                                                            rotateParticlesAsAxis(PI / 90)
                                                        }
                                                    }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .appendPoint(
                        vertices[1].clone()
                    ) {
                        StyleData { it ->
                            ParticleDisplayer.withStyle(
                                getShapeStyle(it).appendPoint(
                                    RelativeLocation()
                                ) {
                                    StyleData {
                                        ParticleDisplayer.withStyle(
                                            getShapeStyle(it)
                                                .appendBuilder(
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
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        if (status == FormationStatus.WORKING) {
                                                            rotateParticlesAsAxis(PI / 128)
                                                        } else {
                                                            rotateParticlesAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }.appendPoint(RelativeLocation()) {
                                    StyleData {
                                        ParticleDisplayer.withStyle(
                                            getShapeStyle(it).appendBuilder(
                                                PointsBuilder().addFourierSeries(
                                                    FourierPresets.circlesAndTriangles().scale(2.0)
                                                        .count(600 * ParticleOption.getParticleCounts())
                                                )
                                            ) {
                                                getSingleStyleDataBuilder().build()
                                            }.toggleOnDisplay {
                                                addPreTickAction {
                                                    if (status == FormationStatus.WORKING) {
                                                        rotateParticlesAsAxis(-PI / 128)
                                                    } else {
                                                        rotateParticlesAsAxis(-PI / 256)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                    .appendPoint(
                        vertices[2].clone()
                    ) {
                        StyleData { it ->
                            ParticleDisplayer.withStyle(
                                getShapeStyle(it)
                                    .appendPoint(RelativeLocation()) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
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
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        if (status == FormationStatus.WORKING) {
                                                            rotateParticlesAsAxis(PI / 128)
                                                        } else {
                                                            rotateParticlesAsAxis(PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }.appendPoint(
                                        RelativeLocation()
                                    ) {
                                        StyleData {
                                            ParticleDisplayer.withStyle(
                                                getShapeStyle(it).appendBuilder(
                                                    PointsBuilder().addFourierSeries(
                                                        FourierPresets.bowsOnAllSides()
                                                            .count(
                                                                480 * ParticleOption.getParticleCounts()
                                                            )
                                                    )
                                                ) {
                                                    getSingleStyleDataBuilder().build()
                                                }.toggleOnDisplay {
                                                    this.addPreTickAction {
                                                        if (status == FormationStatus.WORKING) {
                                                            rotateParticlesAsAxis(-PI / 128)
                                                        } else {
                                                            rotateParticlesAsAxis(-PI / 256)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    .toggleOnDisplay {
                        this.addPreTickAction {
                            rotateParticlesAsAxis(-PI / 256)
                        }
                    }
            )
        }
        val res = HashMap<StyleData, RelativeLocation>()
        res[first] = RelativeLocation()
        return res
    }

    private fun getShapeStyle(uuid: UUID): ParticleShapeStyle {
        val style = ParticleShapeStyle(uuid)
            .loadScaleHelper(0.01, 1.0, 20)
            .toggleOnDisplay {
                reverseFunctionFromStatus(this, this@LargeFormationStyle.statusHelper)
            }
        return style
    }

    private fun getSingleStyleDataBuilder(): StyleDataBuilder {
        val builder = StyleDataBuilder()
        builder.addParticleHandler {
            colorOfRGB(0, 255, 255)
        }
        return builder
    }
}