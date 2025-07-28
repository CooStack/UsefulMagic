package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import java.util.UUID
import kotlin.math.PI

class CopperMagicStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(64.0, uuid) {
    val maxAge = 120
    var current = 0
    var startHide = false

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val style = CopperMagicStyle(uuid)
                .apply {
                    current = args["current"]!!.loadedValue as Int
                    startHide = args["startHide"]!!.loadedValue as Boolean
                }
            return style
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(4.0, 120)
            .withBuilder {
                it
                    .addPolygonInCircle(3, 30, 4.0)
                    .rotateAsAxis(PI / 3)
            }
            .addPolygonInCircle(3, 30, 4.0)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        TestEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(255, 228, 179)
                }
            }
    }

    override fun beforeDisplay(styles: Map<StyleData, RelativeLocation>) {
        scale = 0.1
    }

    override fun onDisplay() {
        addPreTickAction {
            if (!startHide && current++ > maxAge) {
                startHide = true
            }
            if (!startHide && scale < 1.0) {
                scale(scale + 0.1)
            }
            if (startHide) {
                scale(scale - 0.1)
                if (scale <= 0.1) {
                    remove()
                }
            }
            rotateParticlesAsAxis(PI / 36)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf(
            "current" to ParticleControlerDataBuffers.int(current),
            "startHide" to ParticleControlerDataBuffers.boolean(startHide)
        )
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        if (args.containsKey("current")) {
            current = args["current"]!!.loadedValue as Int
        }
        if (args.containsKey("startHide")) {
            startHide = args["startHide"]!!.loadedValue as Boolean
        }
    }
}