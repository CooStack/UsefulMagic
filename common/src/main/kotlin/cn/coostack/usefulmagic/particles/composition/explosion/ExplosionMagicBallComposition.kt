package cn.coostack.usefulmagic.particles.composition.explosion

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.extend.multiply
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.UUID
import kotlin.math.PI
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

@CooAutoRegister
class ExplosionMagicBallComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    @CodecField
    var player: UUID = UUID.randomUUID()

    val option: Int
        get() = ParticleOption.getParticleCounts()

    init {
        setDisabledInterval(30)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addBall(0.5, 5 * option)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withCParticle(it)
                }.addCParticleInstanceInit {
                    effect = ControlableCloudEffect(UUID.randomUUID())
                    color = Math3DUtil.colorOf(160, 120, 255)
                }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            val player = world!!.getPlayerByUUID(player) ?: let {
                status.setStatus(2)
                return@addPreTickAction
            }

            val pos = player.eyePosition.add(player.forward.normalize().multiply(3.0))
            teleportTo(pos)
            rotateAsAxis(PI / 32)
        }
    }

}

