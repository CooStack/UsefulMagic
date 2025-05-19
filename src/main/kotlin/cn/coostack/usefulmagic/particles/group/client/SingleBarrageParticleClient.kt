package cn.coostack.usefulmagic.particles.group.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroup
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroupProvider
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID

class SingleBarrageParticleClient(uuid: UUID) : ControlableParticleGroup(uuid) {

    class Provider : ControlableParticleGroupProvider {
        override fun createGroup(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ControlableParticleGroup {
            return SingleBarrageParticleClient(uuid)
        }

        override fun changeGroup(
            group: ControlableParticleGroup,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ) {
        }

    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun loadParticleLocations(): Map<ParticleRelativeData, RelativeLocation> {
        return mapOf(
            withEffect(
                {
                    ParticleDisplayer.withSingle(
                        ControlableCloudEffect(it)
                    )
                }
            ) {
                maxAge = 120
                colorOfRGB(135, 112, 137)
            }.also {
                it.withControler {
                    withTickDeath = true
                }
            } to RelativeLocation()
        )
    }

    override fun onGroupDisplay() {
    }
}