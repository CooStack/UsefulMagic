package cn.coostack.usefulmagic.particles.fall.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.roundToInt

@CooAutoRegister
class GuideCircleComposition(position: Vec3 = Vec3.ZERO, world: Level? = null) :
    AutoParticleComposition(position, world) {
    @CodecField
    var bindPlayer: UUID = UUID.randomUUID()

    @CodecField
    var r = 8.0

    init {
        visibleRange = 128.0
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(
                r,
                (r * 20 * ParticleOption.getParticleCounts()).roundToInt()
            )
            .createWithCompositionData {
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
                    }
                    .addCParticleInstanceInit {
                        effect = ControlableEndRodEffect(UUID.randomUUID())
                        color.set(150f / 255f, 1f, 1f)
                    }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            val player = world?.getPlayerByUUID(bindPlayer) ?: let {
                remove()
                return@addPreTickAction
            }
            if (player.isDeadOrDying) {
                remove()
                return@addPreTickAction
            }
            val held = player.handSlots.count {
                it.isOf(UsefulMagicItems.SKY_FALLING_RUNE)
            }
            if (held <= 0) {
                remove()
            }
            if (client) return@addPreTickAction
            teleportTo(SkyFallingRuneItem.getTargetLocation(player))
        }
    }
}
