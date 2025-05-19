package cn.coostack.usefulmagic.particles.style.entitiy

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.particles.style.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import java.util.SortedMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI
import kotlin.random.Random

/**
 * level 3
 */
class CraftingLevel3Style(
    uuid: UUID = UUID.randomUUID()
) : SequencedParticleStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return CraftingLevel3Style(uuid)
                .also { it.readPacketArgs(args) }
        }
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()
    val status = HelperUtil.styleStatus(20) as StyleStatusHelper
    private val conditions = ArrayList<Pair<Predicate<CraftingLevel3Style>, Int>>()

    @ControlableBuffer("condition_index")
    private var conditionIndex = 0

    @ControlableBuffer("age")
    var age = 0

    init {
        with(conditions) {
            add(Predicate<CraftingLevel3Style> { age > 1 } to 1)
            add(Predicate<CraftingLevel3Style> { age > 6 } to 1)
            add(Predicate<CraftingLevel3Style> { age > 11 } to 1)
            add(Predicate<CraftingLevel3Style> { age > 16 } to 1)
        }
        status.loadControler(this)
//        alphaHelper.loadControler(this)
    }

    override fun onDisplay() {
        addPreTickAction {
            if (client) return@addPreTickAction
            if (conditionIndex >= conditions.size) {
                return@addPreTickAction
            }
            val (predicate, add) = conditions[conditionIndex]
            if (predicate.test(this@CraftingLevel3Style)) {
                if (add > 0) {
                    addMultiple(add)
                } else {
                    removeMultiple(add)
                }
                conditionIndex++
            }
        }
        addPreTickAction {
            age++
//            if (status.displayStatus != 2) {
//                alphaHelper.increaseAlpha()
//            } else {
//                alphaHelper.decreaseAlpha()
//            }
            rotateParticlesAsAxis(PI / 64)
        }
    }

    override fun getParticlesCount(): Int {
        return 4
    }

    fun ParticleGroupStyle.doWithAlpha(alphaTick: Int = 10) {
        val alphaHelper = HelperUtil.alphaStyle(0.0, 1.0, alphaTick)
        alphaHelper.loadControler(this)
        var reverse = false
        this.addPreTickAction {
            if (!reverse) {
                alphaHelper.increaseAlpha()
            } else {
                alphaHelper.decreaseAlpha()
            }
            if (status.displayStatus == 2) {
                reverse = true
            }
        }
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        val foot = RelativeLocation(0.0, 0.1, 0.0)
        val random = Random(System.currentTimeMillis())
        var order = 0
        fun single(): StyleData = StyleData {
            ParticleDisplayer.withSingle(TestEndRodEffect(it))
        }.withParticleHandler {
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
            colorOfRGB(210, random.nextInt(100, 140), 255)
        }
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(2.0, 30 * options)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 32)
                            }
                        }
                )
            }, order++)
        ] = foot

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(4.0, 45 * options)
                                .addCircle(5.0, 45 * options)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 32)
                            }
                        }
                )
            }, order++)
        ] = foot

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(4, 20 * options, 7.0)
                                .addPolygonInCircle(4, 20 * options, 8.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 20 * options, 7.0)
                                .addPolygonInCircle(4, 20 * options, 8.0)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateParticlesAsAxis(-PI / 32)
                            }
                        }
                )
            }, order++)
        ] = foot

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    getPolygonInCircleVertices(6, 8.0).forEach { origin ->
                                        res.addAll(
                                            PointsBuilder()
                                                .addDiscreteCircleXZ(2.0, 30 * options, 0.1)
                                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                                .rotateAsAxis(PI / 3)
                                                .addCycloidGraphic(2.0, 1.0, -1, 2, 60 * options, 2.0 / 3.0)
                                                .pointsOnEach { it -> it.add(origin) }
                                                .create()
                                        )
                                    }
                                    res
                                }.addCircle(8.0, 120 * options)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 64)
                            }
                        }
                )
            }, order++)
        ] = foot
        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this))
            .apply {
                putAll(status.toArgsPairs())
            }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }

}