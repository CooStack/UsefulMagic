package cn.coostack.usefulmagic.particles.fall.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.UUID

class UpperParticleEmitter(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var bindPlayer = UUID.randomUUID()

    companion object {
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as UpperParticleEmitter
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeUuid(data.bindPlayer)
            }, {
                val instance = UpperParticleEmitter(Vec3d.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance.bindPlayer = it.readUuid()
                instance
            }
        )
    }

    override fun doTick() {

    }

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder()
            
            .create().associateBy { templateData.clone() }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        TODO("Not yet implemented")
    }

    override fun getEmittersID(): String {
        TODO("Not yet implemented")
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        TODO("Not yet implemented")
    }
}