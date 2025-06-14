package cn.coostack.usefulmagic.particles.style.skill

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.util.math.Vec3d
import java.util.UUID

class GiantSwordStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(256.0, uuid) {

    var direction = RelativeLocation()
    val option: Int
        get() = ParticleOption.getParticleCounts()

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return GiantSwordStyle(uuid)
        }

    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addLine(
                Vec3d(-2.0, 0.0, 0.0),
                Vec3d(2.0, 0.0, 0.0),
                20 * option,
            )
            .addLine(
                Vec3d(0.0, -2.0, 0.0),
                Vec3d(0.0, 8.0, 0.0),
                40 * option,
            )
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        TestEndRodEffect(it)
                    )
                }
                    .withParticleHandler {
                        colorOfRGB(255, 255, 255)
                    }
            }
    }

    override fun onDisplay() {
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return ControlableBufferHelper.getPairs(this)
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
    }
}