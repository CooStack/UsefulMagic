package cn.coostack.usefulmagic.particles.style.formation.crystal

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class DefendCrystalStyle(uuid: UUID = UUID.randomUUID()) : CrystalStyle(uuid) {
    override fun displayParticleAnimate() {
        rotateParticlesAsAxis(PI / 256)
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return DefendCrystalStyle(uuid)
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(0.5, 20 * ParticleOption.getParticleCounts())
            .addFourierSeries(
                FourierSeriesBuilder()
                    .scale(0.1)
                    .count(20 * ParticleOption.getParticleCounts())
                    .addFourier(3.0, 2.0, 0.0)
                    .addFourier(2.0, -3.0, 0.0)
            ).createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(0, 255, 255)
                    size = 0.1f
                }
            }

    }
}