package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

@CooAutoRegister
class SingleBarrageComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return mapOf(
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withSingle(
                        ControlableCloudEffect(it)
                    )
                }
                .addParticleInstanceInit {
                    lifetime = 120
                    colorOfRGB(135, 112, 137)
                } to RelativeLocation()
        )
    }

    override fun onDisplay() {
        addPreTickAction {
            toggleRelative()
        }
    }
}







