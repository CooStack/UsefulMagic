package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import java.util.UUID

class EndRodLineStyle(
    @ControlableBuffer("end") var end: RelativeLocation,
    @ControlableBuffer("count") var count: Int,
    @ControlableBuffer("color") var color: Vec3,
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
                1, Vec3.ZERO, 1
            ).also { it.readPacketArgs(args) }
        }

    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val endDir = end.normalize()
        return PointsBuilder()
            .addLine(RelativeLocation().remove(endDir), end.clone().add(endDir), count)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                }.withParticleHandler {
                    colorOfRGB(
                        this@EndRodLineStyle.color.x.toInt(),
                        this@EndRodLineStyle.color.y.toInt(),
                        this@EndRodLineStyle.color.z.toInt()
                    )
                    this.lifetime = this@EndRodLineStyle.maxAge
                    textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
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