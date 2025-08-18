package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ControlableParticleEffect
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import java.util.SortedMap
import java.util.UUID
import kotlin.math.PI

class DiamondWandStyle(val bindPlayer: UUID, uuid: UUID = UUID.randomUUID()) :
    SequencedParticleStyle(64.0, uuid) {
    override fun getParticlesCount(): Int {
        return 7
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val player = args["player"]!!.loadedValue as UUID
            return DiamondWandStyle(player, uuid).also { it.readPacketArgs(args) }
        }

    }

    private val options: Int
        get() = ParticleOption.getParticleCounts()
    private val nextStep = 10
    var age = 0
    val maxAge = 120
    val displayHelper = HelperUtil.styleStatus(20)

    init {
        displayHelper.loadControler(this)
    }

    private fun withSingleStyle(
        effect: (UUID) -> ControlableParticleEffect,
        size: Float,
        alpha: Float,
        color: Vec3,
        textureSheet: ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_LIT,
        anotherInvoker: (ParticleControler) -> Unit = {}
    ): StyleData {
        return StyleData {
            ParticleDisplayer.withSingle(effect.invoke(it))
        }.withParticleHandler {
            this.colorOfRGB(color.x.toInt(), color.y.toInt(), color.z.toInt())
            this.size = size
            this.particleAlpha = alpha
            this.textureSheet = textureSheet
        }.withParticleControlerHandler {
            anotherInvoker(this)
        }
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        var order = 0
        var orderAge = 0
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        val reverseFunction: (ParticleShapeStyle) -> Unit = {
            it.addPreTickAction {
                if (it.scaleReversed && it.scaleHelper!!.isZero()) {
                    it.remove()
                    return@addPreTickAction
                }
                if (displayHelper.displayStatus != 2) {
                    return@addPreTickAction
                }
                if (!it.scaleReversed) {
                    it.scaleReversed(false)
                }
            }
        }
        val h0 = RelativeLocation(0.0, 0.1, 0.0)
        val h1 = RelativeLocation(0.0, 3.0, 0.0)
        val h2 = RelativeLocation(0.0, 10.0, 0.0)
        val h3 = RelativeLocation(0.0, 15.0, 0.0)
        fun ParticleShapeStyle.withShapeStyle(order: Int, rotate: Double): ParticleShapeStyle {
            toggleBeforeDisplay {
                scaleHelper!!.doScaleTo(age - nextStep * order)
            }
                .toggleOnDisplay {
                    addPreTickAction {
                        rotateParticlesAsAxis(rotate)
                    }
                    reverseFunction(this)
                }
            return this
        }
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder().addPoints(listOf(RelativeLocation(0.0, 5.0, 0.0)))
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(2.0, 60 * options)
                                                .addCircle(4.0, 60 * options)
                                        ) {
                                            StyleData {
                                                ParticleDisplayer.withSingle(
                                                    ControlableFireworkEffect(it)
                                                )
                                            }.withParticleHandler {
                                                colorOfRGB(127, 219, 202)
                                                this.currentAge = 0
                                            }
                                        }
                                        .loadScaleHelper(1.0 / 10, 1.0, 10)
                                        .withShapeStyle(orderAge, PI / 32)
                                        .also {
                                            it.addPreTickAction {
                                                val player = getPlayer() ?: return@addPreTickAction
                                                rotateParticlesToPoint(
                                                    RelativeLocation.of(player.forward),
                                                )
                                            }
                                        }
                                )
                            }
                        }.appendBuilder(
                            PointsBuilder().addPoints(listOf(RelativeLocation(0.0, 5.0, 0.0)))
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addPolygonInCircle(4, 10 * options, 5.0)
                                                .rotateAsAxis(PI / 4)
                                                .addPolygonInCircle(4, 10 * options, 5.0)
                                        ) {
                                            StyleData {
                                                ParticleDisplayer.withSingle(
                                                    ControlableFireworkEffect(it)
                                                )
                                            }.withParticleHandler {
                                                colorOfRGB(127, 219, 202)
                                                this.currentAge = 0
                                            }
                                        }.loadScaleHelper(1.0 / 10, 1.0, 10)
                                        .withShapeStyle(orderAge, -PI / 32).also {
                                            it.addPreTickAction {
                                                val player = getPlayer() ?: return@addPreTickAction
                                                rotateParticlesToPoint(
                                                    RelativeLocation.of(player.forward)
                                                )
                                            }
                                        }
                                )
                            }
                        }
                        .appendBuilder(
                            PointsBuilder().addPoints(listOf(RelativeLocation(0.0, 5.0, 0.0)))
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCycloidGraphic(
                                                    2.0, 1.0, -1, 2, 120 * options, 1.0
                                                )
                                                .rotateAsAxis(PI / 3)
                                                .addCycloidGraphic(
                                                    2.0, 1.0, -1, 2, 120 * options, 1.0
                                                )
                                        ) {
                                            StyleData {
                                                ParticleDisplayer.withSingle(
                                                    ControlableFireworkEffect(it)
                                                )
                                            }.withParticleHandler {
                                                colorOfRGB(127, 219, 202)
                                                this.currentAge = 0
                                            }
                                        }.loadScaleHelper(1.0 / 10, 1.0, 10)
                                        .withShapeStyle(orderAge, PI / 64).also {
                                            it.addPreTickAction {
                                                val player = getPlayer() ?: return@addPreTickAction
                                                rotateParticlesToPoint(
                                                    RelativeLocation.of(player.forward),
                                                )
                                            }
                                        }
                                )
                            }
                        }.toggleBeforeDisplay {
                            val player = getPlayer() ?: return@toggleBeforeDisplay
                            preRotateTo(it, RelativeLocation.of(player.forward))
                        }
                        .also {
                            it.addPreTickAction {
                                val player = getPlayer() ?: return@addPreTickAction
                                rotateParticlesToPoint(RelativeLocation.of(player.forward))
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 2.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 20 * options, 2.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 20 * options, 2.0)
                        ) {
                            withSingleStyle(
                                { ControlableEndRodEffect(it) },
                                0.2f,
                                1f,
                                Vec3(100.0, 100.0, 255.0),
                                ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            )
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, PI / 16)
                )
            }, order++)
        ] = h0.clone()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(2.0, 60 * options)
                                .addCircle(3.0, 80 * options)
                        ) {
                            withSingleStyle(
                                { ControlableEndRodEffect(it) },
                                0.2f,
                                1f,
                                Vec3(100.0, 100.0, 255.0),
                                ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            )
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, PI / 16)
                )
            }, order++)
        ] = h0.clone()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(4, 30 * options, 5.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 30 * options, 5.0)
                        ) {
                            withSingleStyle(
                                { ControlableEndRodEffect(it) },
                                0.2f,
                                1f,
                                Vec3(100.0, 100.0, 255.0),
                                ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            )
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, -PI / 16)
                )
            }, order++)
        ] = h0.clone()

        fun setSword(s: EndRodSwordStyle): EndRodSwordStyle {
            var set = false
            s.addPreTickAction {
                val player = getPlayer() ?: return@addPreTickAction
                if (!set) {
                    set = true
                    this.axis = RelativeLocation(0, 0, 1)
                    this.rotateParticlesAsAxis(PI)
                    return@addPreTickAction
                }
                rotateParticlesToPoint(
                    RelativeLocation.of(
                        pos.relativize(
                            Vec3(
                                player.position().x,
                                pos.y,
                                player.position().z
                            )
                        )
                    )
                )
            }
            return s
        }

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircleVertices(8, 10.0)
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(EndRodSwordStyle(it).also { s -> setSword(s) })
                            }
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, PI / 32)
                )
            }, order++)
        ] = h1.clone()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircleVertices(8, 16.0)
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(EndRodSwordStyle(it).also { s -> setSword(s) })
                            }
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, -PI / 32)
                )
            }, order++)
        ] = h2.clone()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    )
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircleVertices(8, 12.0)
                        ) {
                            StyleData {
                                ParticleDisplayer.withStyle(EndRodSwordStyle(it).also { s -> setSword(s) })
                            }
                        }.loadScaleHelper(1 / 20.0, 1.0, 10)
                        .withShapeStyle(orderAge++, PI / 64)
                )
            }, order++)
        ] = h3.clone()
        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf(
            "player" to ParticleControlerDataBuffers.uuid(bindPlayer),
            "age" to ParticleControlerDataBuffers.int(age),
            *displayHelper.toArgsPairs().toTypedArray()
        )
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        displayHelper.readFromServer(args)
    }

    override fun onDisplay() {
        addPreTickAction {
            val player = getPlayer() ?: let {
                remove()
                return@addPreTickAction
            }
            teleportTo(player.position())
            if (client) {
                return@addPreTickAction
            }
            if (age++ % nextStep == 0) {
                addSingle()
            }

            if (!player.useItem.isOf(UsefulMagicItems.DIAMOND_WAND)) {
                displayHelper.setStatus(StatusHelper.Status.DISABLE)
                return@addPreTickAction
            }
        }
    }

    private fun getPlayer(): Player? {
        return world!!.getPlayerByUUID(bindPlayer)
    }
}