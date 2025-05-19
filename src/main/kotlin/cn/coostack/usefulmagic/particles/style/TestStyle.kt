package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.MathUtil
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt

class TestStyle(
    @ControlableBuffer("player")
    val bindPlayer: UUID,
    uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val player = args["player"]!!.loadedValue as UUID
            return TestStyle(player, uuid)
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addBezierCurve(
                RelativeLocation(10.0, 9.5, 0.0),
                RelativeLocation(5.0, 0.0, 0.0),
                RelativeLocation(-5.0, -9.5, 0.0),
                120
            )
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                }.withParticleHandler {
                    val random = Random(System.currentTimeMillis())
                    currentAge = random.nextInt(maxAge)
                }.withParticleControlerHandler {
                    val random = Random(System.currentTimeMillis())
                    this.addPreTickAction {
                        currentAge = random.nextInt(maxAge)
                    }
                }
            }
    }

    override fun beforeDisplay(styles: Map<StyleData, RelativeLocation>) {
//        axis = RelativeLocation.xAxis()
//        preRotateTo(
//            styles,
//            RelativeLocation.of(world!!.getPlayerByUuid(bindPlayer)!!.rotationVector)
//        )
    }

    @ControlableBuffer("current")
    var current = 0
    override fun onDisplay() {
        addPreTickAction {
            current++
            if (current > 120) {
                remove()
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return ControlableBufferHelper.getPairs(this)
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        args["current"]?.let {
            current = it.loadedValue as Int
        }
    }
}