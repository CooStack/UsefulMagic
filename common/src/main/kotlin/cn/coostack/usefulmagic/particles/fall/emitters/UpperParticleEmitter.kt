package cn.coostack.usefulmagic.particles.fall.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.UUID

class UpperParticleEmitter(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var bindPlayer = UUID.randomUUID()

    companion object {
        val CODEC = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as UpperParticleEmitter
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeUUID(data.bindPlayer)
            }, {
                val instance = UpperParticleEmitter(Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance.bindPlayer = it.readUUID()
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
        spawnPos: Vec3,
        spawnWorld: Level
    ) {
        TODO("Not yet implemented")
    }

    override fun getEmittersID(): String {
        TODO("Not yet implemented")
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        TODO("Not yet implemented")
    }
}