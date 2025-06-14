package cn.coostack.usefulmagic.particles.style.entitiy

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.MathPresets
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.awt.Point
import java.util.UUID
import kotlin.math.PI

class BookEntityDeathStyle(
    @ControlableBuffer("bindID") var bindID: Int, uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(256.0, uuid) {

    val option: Int
        get() = ParticleOption.getParticleCounts()

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val id = args["bindID"]!!.loadedValue as Int
            return BookEntityDeathStyle(id, uuid).also {
                it.readPacketArgs(args)
            }
        }
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = HashMap<StyleData, RelativeLocation>()
        fun single(scaleSize: Float = 0.2f): StyleData {
            return StyleData {
                ParticleDisplayer.withSingle(
                    TestEndRodEffect(it)
                )
            }.withParticleHandler {
                colorOfRGB(255, 100, 100)
                size = scaleSize
            }
        }
        // 六芒星
        res[
            StyleData {
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 20 * option, 4.0)
                                .addPolygonInCircle(3, 15 * option, 3.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 20 * option, 4.0)
                                .addPolygonInCircle(3, 15 * option, 3.0)
                        ) {
                            single()
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .toggleOnDisplay {
                            this.reverseFunctionFromStatus(this, statusHelper)
                            this.addPreTickAction {
                                this.rotateParticlesAsAxis(PI / 32.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 圆
        res[
            StyleData {
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(4.0, 60 * option)
                                // 3.5放roma数字
                                .addCircle(5.0, 60 * option)
                        ) {
                            single()
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .toggleOnDisplay {
                            this.reverseFunctionFromStatus(this, statusHelper)
                            this.addPreTickAction {
                                this.rotateParticlesAsAxis(PI / 64.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        res[
            StyleData {
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    getPolygonInCircleVertices(10, 4.5)
                                        .forEachIndexed { index, it ->
                                            res.addAll(
                                                PointsBuilder().addPoints(MathPresets.withRomaNumber(index + 1, 1.0))
                                                    .rotateTo(it)
                                                    .pointsOnEach { p ->
                                                        p.add(it)
                                                    }.create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single(0.1f)
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .toggleOnDisplay {
                            this.reverseFunctionFromStatus(this, statusHelper)
                            this.addPreTickAction {
                                this.rotateParticlesAsAxis(-PI / 32.0)
                            }
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        return res
    }

    val statusHelper = HelperUtil.styleStatus(60)

    init {
        statusHelper.loadControler(this)
    }

    override fun remove() {
        if (statusHelper.displayStatus != 2) {
            statusHelper.setStatus(2)
        } else {
            super.remove()
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            val entity = world?.getEntityById(bindID) ?: let {
                statusHelper.setStatus(2)
                return@addPreTickAction
            }
            teleportTo(entity.pos)
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