package cn.coostack.usefulmagic.particles.group.server

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.ServerParticleGroup
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroup
import cn.coostack.usefulmagic.particles.group.client.EnchantBallBarrageParticleClient
import net.minecraft.world.phys.Vec3

class EnchantBallParticleServer(
    val pColor: Vec3,
    val size: Float,
    val r: Double,
    val countPow: Int,
) : ServerParticleGroup(128.0) {
    override fun tick() {
    }

    override fun otherPacketArgs(): Map<String, ParticleControlerDataBuffer<out Any>> {
        return mapOf(
            "color" to ParticleControlerDataBuffers.vec3d(pColor),
            "size_particle" to ParticleControlerDataBuffers.float(size),
            "r" to ParticleControlerDataBuffers.double(r),
            "count_pow" to ParticleControlerDataBuffers.int(countPow)
        )
    }

    override fun getClientType(): Class<out ControlableParticleGroup>? {
        return EnchantBallBarrageParticleClient::class.java
    }
}