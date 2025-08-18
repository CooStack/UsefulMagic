package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

/**
 * 陨石被施术者魔法阵
 *
 * 召唤3个圆环围绕旋转
 */
class WandMeteoriteTargetStyle(
    @ControlableBuffer("target")
    var target: Int,
    uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(64.0, uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val target = args["target"]!!.loadedValue as Int
            return WandMeteoriteTargetStyle(target, uuid).also {
                it.readPacketArgs(args)
            }
        }

    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    @ControlableBuffer("age")
    var age = 0
    val maxAge = 140

    val statusHelper = HelperUtil.styleStatus(20)

    init {
        statusHelper.loadControler(this)
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = mutableMapOf<StyleData, RelativeLocation>()

        res[
            styleDataWithAxis(RelativeLocation(0.0, 1.0, 0.0), PI / 64)
        ] = RelativeLocation(0.0, 0.1, 0.0)

        res[
            styleDataWithAxis(RelativeLocation(1.0, 0.0, 0.0), -PI / 32)
        ] = RelativeLocation(0.0, 0.1, 0.0)

        res[
            styleDataWithAxis(RelativeLocation(0.0, 0.0, 1.0), PI / 32)
        ] = RelativeLocation(0.0, 0.1, 0.0)

        return res
    }

    private fun styleDataWithAxis(axis: RelativeLocation, rotate: Double): StyleData {
        return StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(2.0, 120 * options)
                    ) {
                        StyleData {
                            ParticleDisplayer.withSingle(
                                ControlableFireworkEffect(it)
                            )
                        }
                            .withParticleHandler {
                                colorOfRGB(240, 100, 100)
                            }
                    }
                    .loadScaleHelperBezierValue(
                        0.1, 1.0, 10,
                        RelativeLocation(1.0, 0.9, 0.0),
                        RelativeLocation(-7.0, 0.0, 0.0),
                    )
                    .toggleOnDisplay {
                        reverseFunctionFromStatus(this, statusHelper as StyleStatusHelper)
                        this.axis = RelativeLocation(1.0, 0.0, 0.0)
                        rotateParticlesToPoint(axis)
                        addPreTickAction {
                            rotateParticlesAsAxis(rotate)
                        }
                    }
            )
        }
    }

    override fun onDisplay() {
        // 锁定实体
        addPreTickAction {
            if (age++ > maxAge && statusHelper.displayStatus != 2) {
                statusHelper.setStatus(StatusHelper.Status.DISABLE)
            }
            val targetEntity = world!!.getEntity(target) ?: return@addPreTickAction
            teleportTo(targetEntity.eyePosition)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this)).also {
            it.putAll(statusHelper.toArgsPairs())
        }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
    }
}