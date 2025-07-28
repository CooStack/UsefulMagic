package cn.coostack.usefulmagic.particles.style.formation.crystal

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class RecoverCrystalStyle(uuid: UUID = UUID.randomUUID()) : CrystalStyle(uuid) {
    override fun displayParticleAnimate() {
        rotateParticlesAsAxis(PI / 256)
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return RecoverCrystalStyle(uuid)
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(0.5, 15 * ParticleOption.getParticleCounts())
            .addPolygonInCircle(6, 5 * ParticleOption.getParticleCounts(), 0.5)
            .addBuilder(
                RelativeLocation(0.0, 0.0, 0.0),
                PointsBuilder().addPolygonInCircle(4, 6 * ParticleOption.getParticleCounts(), 0.5)
                    .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())
            )
            .addBuilder(
                RelativeLocation(0.0, 0.4, 0.0),
                PointsBuilder()
                    .addCircle(0.4, 12 * ParticleOption.getParticleCounts())
            )
            .addBuilder(
                RelativeLocation(0.0, 0.4, 0.0),
                PointsBuilder()
                    .addPolygonInCircle(3, 6 * ParticleOption.getParticleCounts(), 0.4)
            )
            .addBuilder(
                RelativeLocation(0.0, 0.4, 0.0),
                PointsBuilder()
                    .addPolygonInCircle(3, 6 * ParticleOption.getParticleCounts(), 0.4)
                    .rotateAsAxis(0.3333333333333333 * PI, RelativeLocation.yAxis())
            )

            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        TestEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(0, 255, 255)
                    size = 0.05f
                }
            }

    }
}