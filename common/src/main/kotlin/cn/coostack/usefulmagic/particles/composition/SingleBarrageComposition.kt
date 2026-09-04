package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

@CooAutoRegister
class SingleBarrageComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return mapOf(
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                }
                .addCParticleInstanceInit {
                    effect = ControlableCloudEffect(UUID.randomUUID())
                    maxAge = 120
                    color = Math3DUtil.colorOf(135, 112, 137)
                } to RelativeLocation()
        )
    }

    override fun onDisplay() {
        addPreTickAction {
            toggleRelative()
        }
    }
}




