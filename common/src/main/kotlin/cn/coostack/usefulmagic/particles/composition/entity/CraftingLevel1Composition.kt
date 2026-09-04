package cn.coostack.usefulmagic.particles.composition.entity

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.PI

@CooAutoRegister
class CraftingLevel1Composition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var r: Double = 1.0

    @CodecField
    var count: Int = 160

    init {
        status.closedInternal = 20
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(r, count)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.OPAQUE)
                }.addCParticleInstanceInit {
                    effect = ControlableEndRodEffect(UUID.randomUUID())
                    color = Math3DUtil.colorOf(240, 100, 240)
                }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus != 2) {
                playCParticleAlphaTransition(20f, CParticleCurve.linear(0f, 1f))
            } else {
                playCParticleAlphaTransition(20f, CParticleCurve.linear(1f, 0f))
            }
            rotateAsAxis(PI / 64)
        }
    }
}
