package cn.coostack.usefulmagic.particles.style.skill

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import java.util.UUID
import kotlin.math.PI

class TaiChiStyle(uuid: UUID = UUID.randomUUID()) : ParticleGroupStyle(256.0, uuid) {
    val status = HelperUtil.styleStatus(30)

    init {
        status.loadControler(this)
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return TaiChiStyle(uuid).also { it.readPacketArgs(args) }
        }

    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = mutableMapOf<StyleData, RelativeLocation>()
        res[StyleData {
            ParticleDisplayer.Companion.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(0.5, 120)
                            .pointsOnEach {
                                it.x += 1.5
                            }
                    ) {
                        StyleData {
                            ParticleDisplayer.Companion.withSingle(TestEndRodEffect(it))
                        }
                    }.appendBuilder(
                        PointsBuilder()
                            .addCircle(0.5, 120)
                            .pointsOnEach {
                                it.x -= 1.5
                            }
                    ) {
                        StyleData {
                            ParticleDisplayer.Companion.withSingle(TestEndRodEffect(it))
                        }.withParticleHandler {
                            colorOfRGB(0, 0, 0)
                        }
                    }.appendBuilder(
                        PointsBuilder()
                            .addHalfCircle(1.5, 120)
                            .pointsOnEach { p -> p.x += 3.0 }
                            .addHalfCircle(1.5, 120, PI)
                            .pointsOnEach { p -> p.x -= 1.5 }
                    ) {
                        StyleData {
                            ParticleDisplayer.Companion.withSingle(TestEndRodEffect(it))
                        }
                    }.appendBuilder(
                        PointsBuilder()
                            .addCircle(3.0, 240)
                    ) {
                        StyleData {
                            ParticleDisplayer.Companion.withSingle(TestEndRodEffect(it))
                        }
                    }
                    .loadScaleHelper(0.01, 1.0, 20)
                    .toggleOnDisplay {
                        this.reverseFunctionFromStatus(this, status)
                        this.addPreTickAction {
                            rotateParticlesAsAxis(PI / 64)
                        }
                    }
            )
        }] = RelativeLocation(0.0, 0.01, 0.0)
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            rotateParticlesAsAxis(PI / 32)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this))
            .also {
                it.putAll(status.toArgsPairs())
            }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }
}