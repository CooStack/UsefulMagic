package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.buffer.Vec3dControlerBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.util.math.Vec3d
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class EndRodSwordStyle(uuid: UUID = UUID.randomUUID()) : ParticleGroupStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return EndRodSwordStyle(uuid).also { it.readPacketArgs(args) }
        }

    }

    @ControlableBuffer("color")
    var color = Vec3d(255.0, 255.0, 255.0)

    @ControlableBuffer("height")
    var height: Int = 4

    @ControlableBuffer("lowest")
    var lowest: Int = -1

    // 单个宽度
    @ControlableBuffer("weight")
    var weight: Int = 1
    val options: Int
        get() = ParticleOption.getParticleCounts()
    val swordPoints = PointsBuilder()
        .addLine(RelativeLocation(-weight, 0, 0), RelativeLocation(weight, 0, 0), 40 * options)
        .addLine(RelativeLocation(0, lowest, 0), RelativeLocation(0, height, 0), 40 * options)

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return swordPoints.createWithStyleData {
            StyleData {
                ParticleDisplayer.withSingle(
                    TestEndRodEffect(it)
                )
            }.withParticleHandler {
                colorOfRGB(
                    this@EndRodSwordStyle.color.x.toInt(),
                    this@EndRodSwordStyle.color.y.toInt(),
                    this@EndRodSwordStyle.color.z.toInt(),
                )
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