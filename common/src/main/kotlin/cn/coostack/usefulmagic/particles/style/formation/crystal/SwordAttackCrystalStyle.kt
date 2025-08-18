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

class SwordAttackCrystalStyle(uuid: UUID = UUID.randomUUID()) : CrystalStyle(uuid) {
    override fun displayParticleAnimate() {
        rotateParticlesAsAxis(-PI / 256)
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return SwordAttackCrystalStyle(uuid)
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addPolygonInCircle(6, 10, 0.5)
            .addBuilder(
                RelativeLocation(0.0, 0.0, 0.0),
                PointsBuilder().addPolygonInCircle(6, 10, 0.5)
                    .rotateAsAxis(0.16666666666666666 * PI, RelativeLocation.yAxis())
            ).pointsOnEach { it.y += 0.5 }.createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(0, 255, 255)
                    size = 0.05f
                }
            }

    }
}