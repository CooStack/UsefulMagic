package cn.coostack.usefulmagic.particles.style.barrage.wand

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
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.entity.player.PlayerEntity
import java.util.SortedMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI

class AntiEntityWandSpellcasterStyle(
    @ControlableBuffer("player")
    var bindPlayer: UUID,
    uuid: UUID = UUID.randomUUID(),
) : SequencedParticleStyle(256.0, uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return AntiEntityWandSpellcasterStyle(UUID.randomUUID(), uuid)
                .also {
                    it.readPacketArgs(args)
                }
        }

    }

    private val conditions = ArrayList<Pair<Predicate<AntiEntityWandSpellcasterStyle>, Int>>()

    @ControlableBuffer("condition_index")
    private var conditionIndex = 0

    @ControlableBuffer("age")
    var age = 0


    override fun onDisplay() {
        addPreTickAction {
            if (client) return@addPreTickAction
            if (conditionIndex >= conditions.size) {
                return@addPreTickAction
            }
            val (predicate, add) = conditions[conditionIndex]
            if (predicate.test(this@AntiEntityWandSpellcasterStyle)) {
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
            val player = getPlayer() ?: return@addPreTickAction
            if (!client && statusHelper.displayStatus != 2 && !player.activeItem.isOf(UsefulMagicItems.ANTI_ENTITY_WAND)) {
                statusHelper.setStatus(StatusHelper.Status.DISABLE)
            }

            if (statusHelper.displayStatus == 1) {
                scaleHelper.doScale()
            } else {
                scaleHelper.doScaleReversed()
            }

            teleportTo(player.pos)
        }
    }

    override fun getParticlesCount(): Int {
        return 5
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    val scaleHelper = HelperUtil.scaleStyle(1.0 / 60, 1.0, 20)
    val statusHelper = HelperUtil.styleStatus(20)

    init {
        with(conditions) {
            add(Predicate<AntiEntityWandSpellcasterStyle> { age > 1 } to 1)
            add(Predicate<AntiEntityWandSpellcasterStyle> { age > 6 } to 1)
            add(Predicate<AntiEntityWandSpellcasterStyle> { age > 11 } to 2)
            add(Predicate<AntiEntityWandSpellcasterStyle> { age > 16 } to 1)
        }
        scaleHelper.loadControler(this)
        statusHelper.loadControler(this)
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
            if (statusHelper.displayStatus == 2) {
                reverse = true
            }
        }
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        val foot = RelativeLocation(0.0, 0.1, 0.0)
        var order = 0
        fun single(): StyleData = StyleData {
            ParticleDisplayer.withSingle(TestEndRodEffect(it))
        }.withParticleHandler {
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
            colorOfRGB(191, 195, 255)
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
                                .addPolygonInCircle(4, 20 * options, 6.0)
                                .addPolygonInCircle(4, 20 * options, 7.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 20 * options, 6.0)
                                .addPolygonInCircle(4, 20 * options, 7.0)
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

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 40 * options, 10.0)
                                .addPolygonInCircle(3, 40 * options, 11.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 40 * options, 10.0)
                                .addPolygonInCircle(3, 40 * options, 11.0)
                        ) {
                            single()
                        }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            this.doWithAlpha()
                            addPreTickAction {
                                rotateParticlesAsAxis(-PI / 64)
                            }
                        }
                )
            }, order++)
        ] = foot

        return res
    }


    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).apply {
            putAll(
                statusHelper.toArgsPairs()
            )
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
    }

    private fun getPlayer(): PlayerEntity? = world?.getPlayerByUuid(bindPlayer)

}