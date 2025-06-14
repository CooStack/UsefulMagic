package cn.coostack.usefulmagic.particles.style.entitiy

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

/**
 * level 2
 */
class CraftingLevel2Style(
    uuid: UUID = UUID.randomUUID()
) : ParticleGroupStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return CraftingLevel2Style(uuid)
                .also { it.readPacketArgs(args) }
        }
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()
    val alphaHelper = HelperUtil.alphaStyle(0.0, 1.0, 20)
    val status = HelperUtil.styleStatus(20)
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(2.0, 60,0.5)
            .addPolygonInCircle(4, 30, 7.0)
            .addPolygonInCircle(4, 30, 6.0)
            .rotateAsAxis(PI / 4)
            .addPolygonInCircle(4, 30, 7.0)
            .addPolygonInCircle(4, 30, 6.0)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        TestEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(240, 100, 240)
                }
            }
    }

    init {
        status.loadControler(this)
        alphaHelper.loadControler(this)
    }

    override fun onDisplay() {
        status.initHelper()
        addPreTickAction {
            if (status.displayStatus != 2) {
                alphaHelper.increaseAlpha()
            } else {
                alphaHelper.decreaseAlpha()
            }
            rotateParticlesAsAxis(PI / 64)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this))
            .apply {
                putAll(status.toArgsPairs())
            }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }
}