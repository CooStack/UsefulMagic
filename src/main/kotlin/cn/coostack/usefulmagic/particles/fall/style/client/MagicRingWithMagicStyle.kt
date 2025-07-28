package cn.coostack.usefulmagic.particles.fall.style.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class MagicRingWithMagicStyle(uuid: UUID, val parent: SkyFallingStyle) : ParticleGroupStyle(256.0, uuid) {
    var runeSize: Float = 0.2f
    var firstRingRadius = 50.0
    var secondRingRadius = 55.0
    var rotateSpeed = PI / 64
    var subMagicRadius = 4.0
    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 10)
        .apply {
            loadControler(this@MagicRingWithMagicStyle)
        }
    val alpha = HelperUtil.alphaStyle(0.1, 1.0, 10)
        .apply {
            loadControler(this@MagicRingWithMagicStyle)
        }
    val random = Random(System.nanoTime())

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = HashMap<StyleData, RelativeLocation>()
        res.putAll(
            PointsBuilder()
                .addCircle(firstRingRadius, (firstRingRadius * 8 * ParticleOption.getParticleCounts()).toInt())
                .addBuilder(
                    RelativeLocation(), PointsBuilder()
                        .addCircle(
                            secondRingRadius,
                            (secondRingRadius * 8 * ParticleOption.getParticleCounts()).toInt()
                        )
                )
                .createWithStyleData {
                    parent.buildSingleStyle().build()
                })
        val runeRingRadius = (secondRingRadius + firstRingRadius) / 2

        res.putAll(
            PointsBuilder()
                .addFourierSeries(
                    FourierSeriesBuilder()
                        .addFourier(1.0, 3.0)
                        .addFourier(10.0, -12.0)
                        .count((10 * runeRingRadius * ParticleOption.getParticleCounts()).toInt())
                        .scale(0.1 * runeRingRadius)
                )
                .createWithStyleData {
                    parent.buildSingleStyle()
                        .displayer {
                            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                        }
                        .addParticleHandler {
                            size = runeSize
                            currentAge = random.nextInt(0, maxAge)
                        }
                        .build()
                }
        )

        PointsBuilder().addPolygonInCircleVertices(6, runeRingRadius).create().forEach {
            val data = StyleDataBuilder()
                .displayer { it ->
                    ParticleDisplayer.withStyle(
                        parent.buildStyle(it)
                            .appendPoint(
                                RelativeLocation()
                            ) {
                                StyleData { it ->
                                    ParticleDisplayer.withStyle(
                                        SkyFallingSubStyle(it, this.parent)
                                            .apply {
                                                this.r = subMagicRadius
                                            }
                                    )
                                }
                            }
                    )
                }.build()
            res[data] = it
        }
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            rotateParticlesAsAxis(rotateSpeed)
            if (parent.status.displayStatus == 1) {
                alpha.increaseAlpha()
                scaleHelper.doScale()
            } else {
                alpha.decreaseAlpha()
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf()
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {

    }
}