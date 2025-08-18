package cn.coostack.usefulmagic.particles.style.skill

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

class SwordLightStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(256.0, uuid) {
    val status = HelperUtil.styleStatus(20)
    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 20)

    @ControlableBuffer("locked_entity_id")
    var lockedEntityID: Int = -1

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return SwordLightStyle(uuid)
        }
    }

    init {
        status.loadControler(this)
        scaleHelper.loadControler(this)
    }

    val option: Int
        get() = ParticleOption.getParticleCounts()

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(10.0, 180 * option, 3.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .rotateAsAxis(PI / 3)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .addPolygonInCircle(3, 40 * option, 10.0)
            .createWithStyleData {
                StyleData {
                    ParticleDisplayer.withSingle(
                        ControlableEndRodEffect(it)
                    )
                }
                    .withParticleHandler {
                        colorOfRGB(255, 255, 255)
                    }
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (status.displayStatus == 2) {
                scaleHelper.doScaleReversed()
            } else {
                scaleHelper.doScale()
            }
            rotateParticlesAsAxis(PI / 64.0)
            val entity = world!!.getEntity(lockedEntityID)
            if (entity != null && entity.isAlive) {
                val rel = this.pos.relativize(entity.eyePosition.add(0.0, -0.2, 0.0))
                rotateParticlesToPoint(
                    RelativeLocation.of(
                        rel
                    )
                )
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).also {
            it.putAll(
                status.toArgsPairs()
            )
        }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }
}