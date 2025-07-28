package cn.coostack.usefulmagic.particles.style.formation

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class SmallFormationStyle(uuid: UUID = UUID.randomUUID()) : FormationStyle(uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return SmallFormationStyle(uuid)
        }
    }

    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 20)
        .apply {
            loadControler(this@SmallFormationStyle)
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
        val first = StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder().addBuilder(
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
                        StyleData {
                            ParticleDisplayer.withSingle(
                                TestEndRodEffect(it)
                            )
                        }.withParticleHandler {
                            colorOfRGB(0, 255, 255)
                            size = 0.1f
                        }
                    }
            )
        }
        val second = StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it).appendBuilder(
                    PointsBuilder().addBuilder(
                        RelativeLocation(0.0, 1.0, 0.0),
                        PointsBuilder().addCircle(2.5, 50 * ParticleOption.getParticleCounts())

                    )
                        .addCircle(16.0, 180 * ParticleOption.getParticleCounts())
                        .addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 10.0)
                        .addBuilder(
                            RelativeLocation(0.0, 0.0, 0.0),
                            PointsBuilder().addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 10.0)
                                .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())
                        )
                        .addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 12.0)
                        .addBuilder(
                            RelativeLocation(0.0, 0.0, 0.0),
                            PointsBuilder().addPolygonInCircle(4, 30 * ParticleOption.getParticleCounts(), 12.0)
                                .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())
                        )
                        .addBuilder(
                            RelativeLocation(0.0, 0.5, 0.0),
                            PointsBuilder().addCircle(14.0, 180 * ParticleOption.getParticleCounts())
                        ).addFourierSeries(
                            FourierSeriesBuilder()
                                .scale(1.0)
                                .count(314 * ParticleOption.getParticleCounts())
                                .addFourier(3.0, 4.0, 0.0)
                                .addFourier(13.0, -3.0, 0.0)
                        )
                ) {
                    StyleData {
                        ParticleDisplayer.withSingle(
                            TestEndRodEffect(it)
                        )
                    }.withParticleHandler {
                        colorOfRGB(0, 255, 255)
                    }
                }.loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                    reverseFunctionFromStatus(this, this@SmallFormationStyle.statusHelper)
                    this.addPreTickAction {
                        if (status == FormationStatus.WORKING) {
                            rotateParticlesAsAxis(PI / 32)
                        } else {
                            rotateParticlesAsAxis(PI / 256)
                        }
                    }
                }
            )
        }

        val p3 = StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder().addBuilder(
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
                                RelativeLocation(0.0, 0.0, 0.0),
                                PointsBuilder().addPolygonInCircle(4, 50, 6.0)
                                    .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())

                            )
                            .addPolygonInCircle(4, 50, 7.0)
                            .addBuilder(
                                RelativeLocation(0.0, 0.0, 0.0),
                                PointsBuilder().addPolygonInCircle(4, 50, 7.0)
                                    .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())

                            )
                    ) {
                        StyleData { it ->
                            ParticleDisplayer.withSingle(
                                TestEndRodEffect(it)
                            )
                        }.withParticleHandler {
                            colorOfRGB(100, 200, 255)
                        }
                    }.loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                        reverseFunctionFromStatus(this, this@SmallFormationStyle.statusHelper)
                        this.addPreTickAction {
                            if (status == FormationStatus.WORKING) {
                                rotateParticlesAsAxis(-PI / 16)
                            } else {
                                rotateParticlesAsAxis(-PI / 128)
                            }
                        }
                    }
            )
        }

        val res = HashMap<StyleData, RelativeLocation>()
        res[first] = RelativeLocation(0.0, 0.01, 0.0)
        res[second] = RelativeLocation(0.0, 0.01, 0.0)
        res[p3] = RelativeLocation(0.0, 0.01, 0.0)
        return res
    }
}