package cn.coostack.usefulmagic.particles.style.formation

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.MathDataUtil
import cn.coostack.cooparticlesapi.utils.MathPresets
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class MidFormationStyle(uuid: UUID = UUID.randomUUID()) : FormationStyle(uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return MidFormationStyle(uuid)
        }
    }

    override fun displayParticleAnimate() {
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = mutableMapOf<StyleData, RelativeLocation>()

        val n2 = StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it).appendBuilder(
                    PointsBuilder()
                        .addWith {
                            val vertices = Math3DUtil.getPolygonInCircleVertices(12, 29.0)
                            val res = ArrayList<RelativeLocation>()
                            vertices.forEachIndexed { index, origin ->
                                res.addAll(
                                    PointsBuilder()
                                        .addWith {
                                            Math3DUtil.rotatePointsToPoint(
                                                MathPresets.withRomaNumber(index + 1, 3.0),
                                                origin,
                                                RelativeLocation.yAxis(),
                                            )
                                        }.rotateAsAxis(PI, origin)
                                        .create()
                                        .onEach { it ->
                                            it.add(origin)
                                        }
                                )
                            }
                            res
                        }.addCircle(
                            31.0, 720
                        ).addCircle(27.0, 640)
                ) {
                    StyleData { it ->
                        ParticleDisplayer.withSingle(
                            TestEndRodEffect(it)
                        )
                    }.withParticleHandler {
                        colorOfRGB(100, 200, 255)
                    }
                }
                    .loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                        reverseFunctionFromStatus(this, this@MidFormationStyle.statusHelper)
                        this.addPreTickAction {
                            if (status == FormationStatus.WORKING) {
                                rotateParticlesAsAxis(PI / 256)
                            } else {
                                rotateParticlesAsAxis(PI / 512)
                            }
                        }
                    }
            )
        }
        val n1 = StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(32.0, 720)
                            .addBuilder(
                                RelativeLocation(-20.0, 0.0, 0.0),
                                PointsBuilder()
                                    .addCircle(12.0, 480)
                            )
                    ) {
                        StyleData { it ->
                            ParticleDisplayer.withSingle(
                                TestEndRodEffect(it)
                            )
                        }.withParticleHandler {
                            colorOfRGB(100, 200, 255)
                        }
                    }.appendPoint(RelativeLocation(12.0, 0.0, 0.0)) {
                        StyleData { it ->
                            ParticleDisplayer.withStyle(
                                ParticleShapeStyle(it)
                                    .appendBuilder(
                                        PointsBuilder()
                                            .addCircle(20.0, 720)
                                            .rotateAsAxis(PI / 3)
                                            .addPolygonInCircle(6, 100, 20.0)
                                            .rotateAsAxis(PI / 6)
                                            .addPolygonInCircle(6, 100, 20.0)
                                    ) {
                                        StyleData { it ->
                                            ParticleDisplayer.withSingle(
                                                TestEndRodEffect(it)
                                            )
                                        }.withParticleHandler {
                                            colorOfRGB(100, 200, 255)
                                        }
                                    }.loadScaleHelper(0.01, 1.0, 10)
                                    .toggleOnDisplay {
                                        reverseFunctionFromStatus(
                                            this,
                                            this@MidFormationStyle.statusHelper
                                        )
                                        this.addPreTickAction {
                                            if (status == FormationStatus.WORKING) {
                                                rotateParticlesAsAxis(PI / 64)
                                            } else {
                                                rotateParticlesAsAxis(PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    .appendPoint(RelativeLocation(12.0, 0.0, 0.0)) {
                        StyleData { it ->
                            ParticleDisplayer.withStyle(
                                ParticleShapeStyle(it)
                                    .appendPoint(RelativeLocation(3.0, 0.0, 0.0)) {
                                        StyleData { it ->
                                            ParticleDisplayer.withStyle(
                                                ParticleShapeStyle(it).appendBuilder(
                                                    PointsBuilder()
                                                        .addPolygonInCircle(3, 120, 10.0)
                                                        .rotateAsAxis(PI / 3)
                                                        .addPolygonInCircle(3, 120, 10.0)
                                                        .addCircle(10.0, 360)
                                                        .addWith {
                                                            val builder = PointsBuilder()
                                                            PointsBuilder().addPolygonInCircleVertices(3, 10.0)
                                                                .create().forEachIndexed { index, it ->
                                                                    builder.addBuilder(
                                                                        it, PointsBuilder().addCircle(5.0, 240)
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
                                                    StyleData { it ->
                                                        ParticleDisplayer.withSingle(
                                                            TestEndRodEffect(it)
                                                        )
                                                    }.withParticleHandler {
                                                        colorOfRGB(100, 200, 255)
                                                    }
                                                }
                                                    .loadScaleHelper(0.01, 1.0, 20)
                                                    .toggleOnDisplay {
                                                        reverseFunctionFromStatus(
                                                            this,
                                                            this@MidFormationStyle.statusHelper
                                                        )
                                                        this.addPreTickAction {
                                                            if (status == FormationStatus.WORKING) {
                                                                rotateParticlesAsAxis(PI / 64)
                                                            } else {
                                                                rotateParticlesAsAxis(PI / 128)
                                                            }
                                                        }
                                                    }
                                            )
                                        }
                                    }.loadScaleHelper(0.01, 1.0, 20)
                                    .toggleOnDisplay {
                                        reverseFunctionFromStatus(this, this@MidFormationStyle.statusHelper)
                                        this.addPreTickAction {
                                            if (status == FormationStatus.WORKING) {
                                                rotateParticlesAsAxis(-PI / 64)
                                            } else {
                                                rotateParticlesAsAxis(-PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }.appendPoint(RelativeLocation(-20.0, 0.0, 0.0)) {
                        StyleData { it ->
                            ParticleDisplayer.withStyle(
                                ParticleShapeStyle(it).appendBuilder(
                                    PointsBuilder()
                                        .addCircle(11.0, 360)
                                        .addCircle(8.0, 360)
                                        .addWith {
                                            val builder = PointsBuilder()
                                            PointsBuilder().addPolygonInCircleVertices(12, 10.0)
                                                .create().forEachIndexed { index, it ->
                                                    builder.addBuilder(
                                                        it, PointsBuilder().addPoints(
                                                            Math3DUtil.rotatePointsToPoint(
                                                                MathPresets.withRomaNumber(index + 1, 2.0),
                                                                it,
                                                                RelativeLocation.yAxis()
                                                            )
                                                        ).rotateAsAxis(PI, it)
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
                                            RelativeLocation(0.0, 1.5, 0.0), PointsBuilder()
                                                .addDiscreteCircleXZ(5.0, 240, 0.4)
                                                .addPolygonInCircle(4, 120, 5.0)
                                                .rotateAsAxis(PI / 4)
                                                .addPolygonInCircle(4, 120, 5.0)
                                        )
                                ) {
                                    StyleData { it ->
                                        ParticleDisplayer.withSingle(
                                            TestEndRodEffect(it)
                                        )
                                    }.withParticleHandler {
                                        colorOfRGB(100, 200, 255)
                                    }
                                }
                                    .loadScaleHelper(0.01, 1.0, 20)
                                    .toggleOnDisplay {
                                        reverseFunctionFromStatus(this, this@MidFormationStyle.statusHelper)
                                        this.addPreTickAction {
                                            if (status == FormationStatus.WORKING) {
                                                rotateParticlesAsAxis(PI / 64)
                                            } else {
                                                rotateParticlesAsAxis(PI / 128)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    .loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                        reverseFunctionFromStatus(this, this@MidFormationStyle.statusHelper)
                        this.addPreTickAction {
                            if (status == FormationStatus.WORKING) {
                                rotateParticlesAsAxis(-PI / 128)
                            } else {
                                rotateParticlesAsAxis(-PI / 512)
                            }
                        }
                    }
            )
        }
        res[n1] = RelativeLocation(0.0, 0.01, 0.0)
        res[n2] = RelativeLocation(0.0, 0.5, 0.0)
        return res
    }
}