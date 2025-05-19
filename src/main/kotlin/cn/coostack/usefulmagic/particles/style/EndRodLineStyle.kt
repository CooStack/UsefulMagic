package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.util.math.Vec3d
import java.util.UUID

class EndRodLineStyle(
    @ControlableBuffer("end") var end: RelativeLocation,
    @ControlableBuffer("count") var count: Int,
    @ControlableBuffer("color") var color: Vec3d,
    @ControlableBuffer("max") var maxAge: Int,
    uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(128.0, uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return EndRodLineStyle(
                RelativeLocation(),
                1, Vec3d.ZERO, 1
            ).also { it.readPacketArgs(args) }
        }

    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val endDir = end.normalize()
        return PointsBuilder()
            .addLine(RelativeLocation().remove(endDir), end.clone().add(endDir), count)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(TestEndRodEffect(it))
                }.withParticleHandler {
                    colorOfRGB(
                        this@EndRodLineStyle.color.x.toInt(),
                        this@EndRodLineStyle.color.y.toInt(),
                        this@EndRodLineStyle.color.z.toInt()
                    )
                    this.maxAge = this@EndRodLineStyle.maxAge
                    textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
                }.withParticleControlerHandler {
                    addPreTickAction {
                        this.currentAge = age
                    }
                }
            }
    }

    @ControlableBuffer("age")
    var age = 0
    override fun onDisplay() {
        addPreTickAction {
            if (age++ > maxAge) {
                remove()
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return ControlableBufferHelper.getPairs(this)
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
    }
}