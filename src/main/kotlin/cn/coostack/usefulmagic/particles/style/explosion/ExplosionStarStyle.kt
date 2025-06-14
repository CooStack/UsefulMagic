package cn.coostack.usefulmagic.particles.style.explosion

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI

class ExplosionStarStyle(
    @ControlableBuffer("player") var player: UUID,
    uuid: UUID = UUID.randomUUID()
) : SequencedParticleStyle(256.0, uuid) {
    @ControlableBuffer("age")
    var age = 0
    val maxAge = 20

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return ExplosionStarStyle(
                args["player"]!!.loadedValue as UUID,
                uuid
            ).also {
                it.readPacketArgs(args)
            }
        }

    }

    val statusHelper = HelperUtil.styleStatus(1)

    init {
        statusHelper.loadControler(this)
    }


    override fun remove() {
        if (statusHelper.displayStatus == 2) {
            super.remove()
        } else {
            statusHelper.setStatus(2)
        }
    }
    // 十字 + 圆环
    override fun getParticlesCount(): Int {
        return 2
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    val random = Random(System.currentTimeMillis())
    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 1
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addLine(
                                    RelativeLocation(-0.5, 0.0, 0.0),
                                    RelativeLocation(0.5, 0.0, 0.0),
                                    options * 2
                                )
                                .addLine(
                                    RelativeLocation(0.0, 0.35, 0.0),
                                    RelativeLocation(0.0, -0.35, 0.0),
                                    options * 2
                                )
                        ) { it ->
                            StyleData { it ->
                                ParticleDisplayer.withSingle(
                                    ControlableFireworkEffect(it)
                                )
                            }.withParticleHandler {
                                this.size = 0.1f
                                colorOfRGB(
                                    random.nextInt(170, 255),
                                    random.nextInt(180, 255),
                                    random.nextInt(230, 255),
                                )
                            }
                        }
                        .toggleOnDisplay {
                            this.axis = RelativeLocation.zAxis()
                            var subAge = 0
                            val player = world!!.getPlayerByUuid(player) ?: return@toggleOnDisplay
                            val dir = pos.relativize(player.eyePos)
                            rotateParticlesToPoint(RelativeLocation.of(dir))
                            addPreTickAction {
                                subAge++
                                if (subAge >= 10) {
                                    scaleReversed(false)
                                }
                                rotateParticlesAsAxis(PI / 8)
                            }
                        }
                        .loadScaleHelperBezierValue(
                            0.01, 1.0, 10,
                            RelativeLocation(5.0, 0.99, 0.0),
                            RelativeLocation(-5.0, 0.0, 0.0),
                        )
                )
            }, order++)
        ] = RelativeLocation(0.0, 0.01, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(
                                    0.4, 5 * options
                                )
                        ) { it ->
                            StyleData { it ->
                                ParticleDisplayer.withSingle(
                                    ControlableFireworkEffect(it)
                                )
                            }.withParticleHandler {
                                colorOfRGB(
                                    random.nextInt(230, 255),
                                    random.nextInt(130, 180),
                                    random.nextInt(130, 180),
                                )
                            }
                        }
                        .toggleOnDisplay {
                            val player = world!!.getPlayerByUuid(player) ?: return@toggleOnDisplay
                            val dir = pos.relativize(player.eyePos)
                            rotateParticlesToPoint(RelativeLocation.of(dir))
                            var currentTick = 0
                            addPreTickAction {
                                rotateParticlesAsAxis(-PI / 8)
                                currentTick++
                                if (currentTick >= 5) {
                                    scaleReversed(false)
                                }
                            }
                        }
                        .loadScaleHelperBezierValue(
                            0.0001, 1.0, 5,
                            RelativeLocation(2.5, 0.99, 0.0),
                            RelativeLocation(-2.5, 0.0, 0.0),
                        )
                )
            }, order++)
        ] = RelativeLocation(0.0, 0.01, 0.0)
        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this)).also {
            it.putAll(statusHelper.toArgsPairs())
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        statusHelper.readFromServer(args)
    }

    override fun onDisplay() {
        addPreTickAction {
            if (age == 1 && !client) {
                addSingle()
            }
            if (age++ > maxAge) {
                statusHelper.setStatus(2)
            }
            if (age == maxAge / 2 && !client) {
                addSingle()
            }
        }
    }
}