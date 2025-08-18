package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.UUID
import kotlin.math.PI

/**
 * 陨石施术者自身的魔法阵
 */
class WandMeteoriteSpellcasterStyle(
    @ControlableBuffer("player")
    var bindPlayer: UUID,
    uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(128.0, uuid) {
    val statusHelper = HelperUtil.styleStatus(20)
    val options: Int
        get() = ParticleOption.getParticleCounts()

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val player = args["player"]!!.loadedValue as UUID
            return WandMeteoriteSpellcasterStyle(player, uuid).also {
                it.readPacketArgs(args)
            }
        }

    }

    init {
        statusHelper.loadControler(this)
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val points = HashMap<StyleData, RelativeLocation>()
        val foot = RelativeLocation(0.0, 0.1, 0.0)
        points[
            withShapeStyle {
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCycloidGraphic(
                                1.0, 10.0, 3, -7, 270 * options, 0.5
                            )
                    ) {
                        redColorStyleData()
                    }
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(4.0, 120 * options)
                    ) {
                        redColorStyleData()
                    }
                    .loadScaleHelperBezierValue(
                        1 / 20.0, 1.0, 20,
                        RelativeLocation(1.0, 1.0 - 1 / 20.0, 0.0),
                        RelativeLocation(-18.0, 0.0, 0.0),
                    )
                    .toggleOnDisplay {
                        reverseFunctionFromStatus(this, statusHelper as StyleStatusHelper)
                        addPreTickAction {
                            rotateParticlesAsAxis(PI / 32)
                        }
                    }
            }
        ] = foot.clone()

        points[
            withShapeStyle {
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCycloidGraphic(
                                2.0, 1.0, -1, 2, 60 * options, 4.0 / 3.0
                            )
                            .rotateAsAxis(PI / 3)
                            .addCycloidGraphic(
                                2.0, 1.0, -1, 2, 60 * options, 4.0 / 3.0
                            )
                    ) {
                        redColorStyleData()
                    }
                    .loadScaleHelperBezierValue(
                        1 / 20.0, 1.0, 20,
                        RelativeLocation(1.0, 1.0 - 1 / 20.0, 0.0),
                        RelativeLocation(-18.0, 0.0, 0.0),
                    )
                    .toggleOnDisplay {
                        reverseFunctionFromStatus(this, statusHelper as StyleStatusHelper)
                        addPreTickAction {
                            rotateParticlesAsAxis(-PI / 32)
                        }
                    }
            }
        ] = foot.clone()

        return points
    }

    private fun redColorStyleData(): StyleData {
        return StyleData {
            ParticleDisplayer.withSingle(
                ControlableEndRodEffect(it)
            )
        }
            .withParticleHandler {
                colorOfRGB(255, 60, 100)
            }

    }

    fun withShapeStyle(shapeBuilder: (UUID) -> ParticleShapeStyle): StyleData {
        return StyleData {
            ParticleDisplayer.withStyle(shapeBuilder(it))
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            val player = world!!.getPlayerByUUID(bindPlayer) ?: return@addPreTickAction
            // 手中必须激活物品 否则设置为2
            teleportTo(player.position())
            if (!client && statusHelper.displayStatus != 2 && !player.useItem.isOf(UsefulMagicItems.WAND_OF_METEORITE)) {
                statusHelper.setStatus(StatusHelper.Status.DISABLE)
            }
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