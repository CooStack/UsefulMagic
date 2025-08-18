package cn.coostack.usefulmagic.particles.style.explosion

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.extend.multiply
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class ExplosionMagicBallStyle(
    @ControlableBuffer("player") var player: UUID,
    uuid: UUID = UUID.randomUUID()
) : ParticleGroupStyle(256.0, uuid) {
    val option: Int
        get() = ParticleOption.getParticleCounts()

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return ExplosionMagicBallStyle(
                args["player"]!!.loadedValue as UUID, uuid,
            ).also {
                it.readPacketArgs(args)
            }
        }

    }

    val statusHelper = HelperUtil.styleStatus(30)

    init {
        statusHelper.loadControler(this)
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addBall(0.5, 5 * option)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        ControlableCloudEffect(it)
                    )
                }.withParticleHandler {
                    colorOfRGB(160, 120, 255)
                }
            }
    }


    override fun onDisplay() {
        addPreTickAction {
            val player = world!!.getPlayerByUUID(player) ?: let {
                statusHelper.setStatus(2)
                return@addPreTickAction
            }

            val pos = player.eyePosition.add(player.forward.normalize().multiply(3.0))
            teleportTo(pos)
            rotateParticlesAsAxis(PI / 32)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).also {
            it.putAll(statusHelper.toArgsPairs())
        }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
    }
}