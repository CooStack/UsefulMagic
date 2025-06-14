package cn.coostack.usefulmagic.particles.style.skill

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class
BookShootSkillStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(256.0, uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return BookShootSkillStyle(uuid).also {
                it.readPacketArgs(args)
            }
        }

    }

    val status = HelperUtil.styleStatus(20)
    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 20)

    init {
        scaleHelper.loadControler(this)
        status.loadControler(this)
    }

    val option = ParticleOption.getParticleCounts()
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(5.0, 120 * option)
            .addPolygonInCircle(3, 10 * option, 5.0)
            .rotateAsAxis(PI / 3)
            .addPolygonInCircle(3, 10 * option, 5.0)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        TestEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(255, 200, 100)
                    size = 0.3f
                }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus != 2) {
                scaleHelper.doScale()
            } else {
                scaleHelper.doScaleReversed()
            }
            rotateParticlesAsAxis(PI / 64)
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