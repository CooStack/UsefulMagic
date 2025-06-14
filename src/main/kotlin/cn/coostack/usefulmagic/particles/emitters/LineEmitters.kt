package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class LineEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    /**
     * 传入相对位置
     */
    var endPos: Vec3d = Vec3d.ZERO
    var count: Int = 10

    companion object {
        const val ID = "line-emitters"
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as LineEmitters
                encodeBase(data, buf)
                ControlableParticleData.Companion.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeVec3d(data.endPos)
                buf.writeInt(data.count)
            }, {
                val container = LineEmitters(Vec3d.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.Companion.PACKET_CODEC.decode(it)
                container.endPos = it.readVec3d()
                container.count = it.readInt()
                container
            }
        )

    }

    override fun doTick() {
    }

    val option: Int
        get() = ParticleOption.getParticleCounts()

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder()
            .addLine(Vec3d.ZERO, endPos, count * option)
            .create().associateBy {
                templateData.clone()
            }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}