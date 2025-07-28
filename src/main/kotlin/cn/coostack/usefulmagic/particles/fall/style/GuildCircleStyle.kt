package cn.coostack.usefulmagic.particles.fall.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.roundToInt

class GuildCircleStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(128.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return GuildCircleStyle()
        }
    }

    @ControlableBuffer("bind_player")
    var bindPlayer: UUID = UUID.randomUUID()

    @ControlableBuffer("radius")
    var r = 8.0
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(r, (r * 20 * ParticleOption.getParticleCounts()).roundToInt())
            .createWithStyleData {
                StyleDataBuilder()
                    .addParticleHandler {
                        colorOfRGB(150, 255, 255)
                    }
                    .build()
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            val player = world?.getPlayerByUuid(bindPlayer) ?: let {
                remove()
                return@addPreTickAction
            }
            if (player.isDead) {
                remove()
                return@addPreTickAction
            }
            val held = player.handItems.count {
                it.isOf(UsefulMagicItems.SKY_FALLING_RUNE)
            }
            if (held <= 0) {
                remove()
            }
            if (client) return@addPreTickAction
            teleportTo(SkyFallingRuneItem.getTargetLocation(player))
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return ControlableBufferHelper.getPairs(this)
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
    }
}