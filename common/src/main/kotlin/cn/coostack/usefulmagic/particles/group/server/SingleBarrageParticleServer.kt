package cn.coostack.usefulmagic.particles.group.server

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.ServerParticleGroup
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroup
import cn.coostack.usefulmagic.particles.group.client.SingleBarrageParticleClient

class SingleBarrageParticleServer(
) : ServerParticleGroup(64.0) {
    override fun tick() {
    }

    override fun otherPacketArgs(): Map<String, ParticleControlerDataBuffer<out Any>> {
        return mapOf()
    }

    override fun getClientType(): Class<out ControlableParticleGroup> {
        return SingleBarrageParticleClient::class.java
    }
}