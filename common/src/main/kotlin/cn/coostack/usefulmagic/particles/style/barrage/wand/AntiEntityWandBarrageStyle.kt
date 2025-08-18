package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import java.util.UUID

class AntiEntityWandBarrageStyle(uuid: UUID = UUID.randomUUID()) :

    ParticleGroupStyle(256.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return AntiEntityWandBarrageStyle(uuid)
        }

    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return mapOf(
            StyleData {
                ParticleDisplayer.withSingle(
                    ControlableFireworkEffect(it)
                )
            }.withParticleHandler {
                colorOfRGB(252, 252, 252)
            } to RelativeLocation()
        )
    }

    override fun onDisplay() {
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf()
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
    }
}