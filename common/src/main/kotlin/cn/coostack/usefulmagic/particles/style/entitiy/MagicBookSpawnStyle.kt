package cn.coostack.usefulmagic.particles.style.entitiy

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.ArrayList
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI

class MagicBookSpawnStyle(uuid: UUID = UUID.randomUUID()) :
    SequencedParticleStyle(256.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return MagicBookSpawnStyle(uuid)
        }
    }

    private val animationConditions = ArrayList<Pair<Predicate<MagicBookSpawnStyle>, Int>>()
    var animationIndex = 0
        private set
    val status = HelperUtil.styleStatus(60)

    @ControlableBuffer("age")
    var age = 0

    init {
        status.loadControler(this)
        animationConditions.add(
            Predicate<MagicBookSpawnStyle> {
                age > 1
            } to 1
        )
        animationConditions.add(
            Predicate<MagicBookSpawnStyle> {
                age > 5
            } to 1
        )
        animationConditions.add(
            Predicate<MagicBookSpawnStyle> {
                age > 10
            } to 1
        )
    }

    override fun getParticlesCount(): Int {
        return 3
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 0
        fun single(): StyleData {
            return StyleData {
                ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
            }
        }
        // 第一层 小圆+ 六芒星
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(2.0, 60 * options)
                                .addCycloidGraphic(1.0, 2.0, 2, -1, 120 * options, 2 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(1.0, 2.0, 2, -1, 120 * options, 2 / 3.0)
                        ) {
                            single().withParticleHandler {
                                colorOfRGB(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20)
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 64)
                            }
                            reverseFunctionFromStatus(this, status)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 几何图形符文
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(6.0, 80 * options)
                                .addPolygonInCircle(6, 20 * options, 4.0)
                                .addWith {
                                    val res = arrayListOf<RelativeLocation>()
                                    PointsBuilder()
                                        .addPolygonInCircleVertices(6, 4.0)
                                        .create()
                                        .forEachIndexed { index, origin ->
                                            res.addAll(
                                                PointsBuilder()
                                                    .addPolygonInCircle(index + 3, 20 * options, 2.0)
                                                    .rotateAsAxis(PI / (index + 1))
                                                    .pointsOnEach { it -> it.add(origin) }
                                                    .create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single().withParticleHandler {
                                colorOfRGB(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(-PI / 48)
                            }
                            reverseFunctionFromStatus(this, status)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 大八芒星+大圆
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(10.0, 80 * options)
                                .addPolygonInCircle(4, 30 * options, 10.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 30 * options, 10.0)
                                .addWith {
                                    val res = arrayListOf<RelativeLocation>()
                                    PointsBuilder()
                                        .addPolygonInCircleVertices(4, 10.0)
                                        .create()
                                        .forEach { origin ->
                                            res.addAll(
                                                PointsBuilder()
                                                    .addCircle(2.0, 30 * options)
                                                    .addPolygonInCircle(3, 8 * options, 2.0)
                                                    .rotateAsAxis(PI / 3)
                                                    .addPolygonInCircle(3, 8 * options, 2.0)
                                                    .pointsOnEach { it ->
                                                        it.add(origin)
                                                    }
                                                    .create()
                                            )
                                        }
                                    res
                                }
                        ) {
                            single().withParticleHandler {
                                colorOfRGB(255, 100, 230)
                            }
                        }.loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 32)
                            }
                            reverseFunctionFromStatus(this, status)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 0.1, 0.0)


        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).also {
            it.putAll(status.toArgsPairs())
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }

    override fun remove() {
        if (status.displayStatus == 2) {
            super.remove()
        } else {
            status.setStatus(2)
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            age++
            if (animationIndex >= animationConditions.size) {
                return@addPreTickAction
            }
            val (predicate, add) = animationConditions[animationIndex]
            if (predicate.test(this@MagicBookSpawnStyle)) {
                if (add > 0) {
                    addMultiple(add)
                } else {
                    removeMultiple(add)
                }
                animationIndex++
            }
        }


    }
}