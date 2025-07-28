package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ControlableParticleEffect
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.utils.MathUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.HashMap
import java.util.SortedMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI

class NetheriteWandStyle(val bindPlayer: UUID, uuid: UUID = UUID.randomUUID()) :
    SequencedParticleStyle(64.0, uuid) {


    private val conditions = ArrayList<Pair<Predicate<NetheriteWandStyle>, Int>>()

    @ControlableBuffer("condition_index")
    private var conditionIndex = 0
    override fun getParticlesCount(): Int {
        return 3
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val player = args["player"]!!.loadedValue as UUID
            return NetheriteWandStyle(player, uuid).also { it.readPacketArgs(args) }
        }

    }

    private val options: Int
        get() = ParticleOption.getParticleCounts()

    var age = 0
    val maxAge = 120
    val displayHelper = HelperUtil.styleStatus(20)

    init {
        displayHelper.loadControler(this)
        with(conditions) {
            add(Predicate<NetheriteWandStyle> {
                it.age > 1
            } to 2)
            add(Predicate<NetheriteWandStyle> {
                it.age > 20
            } to 1)
        }

    }

    private fun withSingleStyle(
        order: Int,
        effect: (UUID) -> ControlableParticleEffect,
        size: Float,
        alpha: Float,
        color: Vec3d,
        textureSheet: ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_LIT,
        anotherInvoker: (ParticleControler) -> Unit = {}
    ): SortedStyleData {
        return SortedStyleData({
            ParticleDisplayer.withSingle(effect.invoke(it))
        }, order).withParticleHandler {
            this.colorOfRGB(color.x.toInt(), color.y.toInt(), color.z.toInt())
            this.size = size
            this.particleAlpha = alpha
            this.textureSheet = textureSheet
        }.withParticleControlerHandler {
            anotherInvoker(this)
        } as SortedStyleData
    }

    fun ParticleShapeStyle.fastRotateToPlayerBefore(
        styles: Map<StyleData, RelativeLocation>,
        player: PlayerEntity
    ): ParticleShapeStyle {
        val to = RelativeLocation.of(player.rotationVector)
        Math3DUtil.rotatePointsToPoint(
            styles.values.toList(), to.clone(), this.axis
        )
        this.axis = to.clone()
        return this
    }

    fun SequencedParticleShapeStyle.fastRotateToPlayerBefore(
        styles: SortedMap<SortedStyleData, RelativeLocation>,
        player: PlayerEntity
    ): SequencedParticleShapeStyle {
        val to = RelativeLocation.of(player.rotationVector)
        Math3DUtil.rotatePointsToPoint(
            styles.values.toList(), to.clone(), this.axis
        )
        this.axis = to.clone()
        return this
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
            if (displayHelper.displayStatus == 2) {
                reverse = true
            }
        }
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        var order = 0
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        val foot = RelativeLocation(0.0, 0.01, 0.0)
        val header = RelativeLocation(0.0, 12.0, 0.0)
        val h3 = RelativeLocation(0.0, 8.0, 0.0)
        val h4 = RelativeLocation(0.0, 4.0, 0.0)
        val h5 = RelativeLocation(0.0, 2.0, 0.0)
        fun single(): StyleData = StyleData {
            ParticleDisplayer.withSingle(TestEndRodEffect(it))
        }.withParticleHandler {
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
            colorOfRGB(200, 100, 200)
        }
        // 玩家视野前
        res[
            SortedStyleData({
                var subOrder = 0
                ParticleDisplayer.withStyle(
                    SequencedParticleShapeStyle(it)
                        .appendAnimateCondition({ it -> it.spawnAge >= 1 }, 2)
                        .appendAnimateCondition({ it -> it.spawnAge >= 30 }, 1)
                        .appendPoint(
                            RelativeLocation(0.0, 3.0, 0.0)
                        ) {
                            SortedStyleData({
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .loadScaleHelper(0.1, 1.0, 10)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(4.0, 60 * options)
                                                .addPolygonInCircle(3, 10 * options, 4.0)
                                                .rotateAsAxis(PI / 3)
                                                .addPolygonInCircle(3, 10 * options, 4.0)
                                                .addDiscreteCircleXZ(5.5, 60 * options, 0.5)
                                        ) {
                                            single()
                                        }
                                        .loadScaleHelper(1 / 20.0, 1.0, 5)
                                        .toggleOnDisplay {
                                            doWithAlpha()
                                            this.reverseFunctionFromStatus(
                                                this,
                                                displayHelper as StyleStatusHelper
                                            )
                                            this.addPreTickAction {
                                                val player = getPlayer() ?: return@addPreTickAction
                                                this.rotateToWithAngle(
                                                    RelativeLocation.of(player.rotationVector),
                                                    PI / 32
                                                )
                                            }
                                        }
                                )
                            }, subOrder++)
                        }
                        .appendPoint(RelativeLocation(0.0, 3.0, 0.0)) {
                            SortedStyleData(
                                {
                                    ParticleDisplayer.withStyle(
                                        ParticleShapeStyle(it)
                                            .appendBuilder(
                                                PointsBuilder()
                                                    .addWith {
                                                        val res = ArrayList<RelativeLocation>()
                                                        getPolygonInCircleVertices(3, 3.5).forEach { origin ->
                                                            res.addAll(
                                                                PointsBuilder()
                                                                    .addCircle(1.5, 30 * options)
                                                                    .addPolygonInCircle(3, 10 * options, 1.5)
                                                                    .rotateAsAxis(PI / 3)
                                                                    .addPolygonInCircle(3, 10 * options, 1.5)
                                                                    .pointsOnEach {
                                                                        it.add(origin)
                                                                    }
                                                                    .create()
                                                            )
                                                        }
                                                        res
                                                    }
                                            ) { single() }
                                            .loadScaleHelper(1 / 20.0, 1.0, 5)
                                            .toggleOnDisplay {
                                                doWithAlpha()
                                                this.reverseFunctionFromStatus(
                                                    this,
                                                    displayHelper as StyleStatusHelper
                                                )
                                                this.addPreTickAction {
                                                    val player = getPlayer() ?: return@addPreTickAction
                                                    rotateToWithAngle(
                                                        RelativeLocation.of(player.rotationVector),
                                                        PI / 32
                                                    )
                                                }
                                            }
                                    )
                                }, subOrder++
                            )
                        }
                        .appendPoint(RelativeLocation(0.0, 3.0, 0.0)) {
                            SortedStyleData({
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendPoint(RelativeLocation(0.0, 2.0, 0.0)) {
                                            StyleData {
                                                ParticleDisplayer.withStyle(
                                                    ParticleShapeStyle(it)
                                                        .appendBuilder(
                                                            PointsBuilder()
                                                                .addDiscreteCircleXZ(8.0, 120 * options, 0.6)
                                                        ) {
                                                            single()
                                                        }
                                                        .toggleOnDisplay {
                                                            doWithAlpha()
                                                            this.reverseFunctionFromStatus(
                                                                this,
                                                                displayHelper as StyleStatusHelper
                                                            )
                                                            this.addPreTickAction {
                                                                val player = getPlayer() ?: return@addPreTickAction
                                                                rotateToWithAngle(
                                                                    RelativeLocation.of(player.rotationVector),
                                                                    -PI / 32
                                                                )
                                                            }
                                                        }
                                                )
                                            }
                                        }.appendPoint(RelativeLocation(0.0, 6.0, 0.0)) {
                                            StyleData {
                                                ParticleDisplayer.withStyle(
                                                    ParticleShapeStyle(it)
                                                        .appendBuilder(
                                                            PointsBuilder()
                                                                .addDiscreteCircleXZ(4.0, 80 * options, 0.3)
                                                        ) {
                                                            single()
                                                        }
                                                        .toggleOnDisplay {
                                                            doWithAlpha()
                                                            this.reverseFunctionFromStatus(
                                                                this,
                                                                displayHelper as StyleStatusHelper
                                                            )
                                                            this.addPreTickAction {
                                                                val player = getPlayer() ?: return@addPreTickAction
                                                                rotateToWithAngle(
                                                                    RelativeLocation.of(player.rotationVector),
                                                                    -PI / 32
                                                                )
                                                            }
                                                        }
                                                )
                                            }
                                        }.appendPoint(RelativeLocation(0.0, 10.0, 0.0)) {
                                            StyleData {
                                                ParticleDisplayer.withStyle(
                                                    ParticleShapeStyle(it)
                                                        .appendBuilder(
                                                            PointsBuilder()
                                                                .addDiscreteCircleXZ(2.0, 40 * options, 0.2)
                                                                .addCycloidGraphic(
                                                                    2.0,
                                                                    1.0,
                                                                    1,
                                                                    -2,
                                                                    60 * options,
                                                                    2.0 / 3.0
                                                                )
                                                                .rotateAsAxis(PI / 3)
                                                                .addCycloidGraphic(
                                                                    2.0,
                                                                    1.0,
                                                                    1,
                                                                    -2,
                                                                    60 * options,
                                                                    2.0 / 3.0
                                                                )
                                                        ) {
                                                            single()
                                                        }
                                                        .toggleOnDisplay {
                                                            doWithAlpha()
                                                            this.reverseFunctionFromStatus(
                                                                this,
                                                                displayHelper as StyleStatusHelper
                                                            )
                                                            this.addPreTickAction {
                                                                val player = getPlayer() ?: return@addPreTickAction
                                                                rotateToWithAngle(
                                                                    RelativeLocation.of(player.rotationVector),
                                                                    -PI / 32
                                                                )
                                                            }
                                                        }
                                                )
                                            }
                                        }.loadScaleHelperBezierValue(
                                            0.1, 1.0, 20,
                                            RelativeLocation(2.0, 0.9, 0.0),
                                            RelativeLocation(-15.0, 0.0, 0.0),
                                        )
                                        .toggleBeforeDisplay {
                                            fastRotateToPlayerBefore(it, getPlayer() ?: return@toggleBeforeDisplay)
                                        }
                                        .toggleOnDisplay {
                                            this.reverseFunctionFromStatus(
                                                this,
                                                displayHelper as StyleStatusHelper
                                            )
                                            this.addPreTickAction {
                                                val player = getPlayer() ?: return@addPreTickAction
                                                rotateParticlesToPoint(
                                                    RelativeLocation.of(player.rotationVector),
                                                )
                                            }
                                        }
                                )
                            }, subOrder++)
                        }
                        .toggleBeforeDisplay {
                            fastRotateToPlayerBefore(it, getPlayer() ?: return@toggleBeforeDisplay)
                        }
                        .toggleOnDisplay {
                            this.addPreTickAction {
                                val player = getPlayer() ?: return@addPreTickAction
                                rotateParticlesToPoint(RelativeLocation.of(player.rotationVector))
                            }
                        }
                )
            }, order++)
        ] = h5.clone()

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(MathUtil.getPolygonInscribedCircle(3, 3.5), 30 * options)
                                .addCircle(MathUtil.getPolygonInscribedCircle(3, 4.5), 35 * options)
                                .addPolygonInCircle(
                                    3, 20 * options,
                                    4.5
                                ).addPolygonInCircle(
                                    3, 30 * options,
                                    6.5
                                ).addWith {
                                    val res = arrayListOf<RelativeLocation>()
                                    PointsBuilder().addPolygonInCircleVertices(
                                        3,
                                        6.5
                                    ).create().forEach { it ->
                                        res.addAll(
                                            PointsBuilder()
                                                .addCircle(1.0, 10 * options)
                                                .addCircle(1.5, 20 * options)
                                                .pointsOnEach { p -> p.add(it) }
                                                .create()
                                        )
                                    }
                                    res
                                }
                        ) {
                            single()
                        }.loadScaleHelperBezierValue(
                            0.01, 1.0, 20,
                            RelativeLocation(2.0, 0.99, 0.0),
                            RelativeLocation(-10.0, 0.0, 0.0),
                        )
                        .toggleOnDisplay {
                            this.reverseFunctionFromStatus(this, displayHelper as StyleStatusHelper)
                            doWithAlpha(20)
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
                                .addCircle(4.5, 60 * options)
                                .addCircle(5.0, 60 * options)
                                .addCircle(6.5, 90 * options)
                                .addCircle(7.0, 90 * options)
                                .addPolygonInCircle(3, 30 * options, 7.5)
                                .rotateAsAxis(PI / 3)
                        ) {
                            single()
                        }.loadScaleHelperBezierValue(
                            0.01, 1.0, 20,
                            RelativeLocation(2.0, 0.99, 0.0),
                            RelativeLocation(-10.0, 0.0, 0.0),
                        )
                        .toggleOnDisplay {
                            this.reverseFunctionFromStatus(this, displayHelper as StyleStatusHelper)
                            doWithAlpha(20)
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
        return HashMap(ControlableBufferHelper.getPairs(this))
            .apply {
                putAll(
                    mapOf(
                        "player" to ParticleControlerDataBuffers.uuid(bindPlayer),
                        "age" to ParticleControlerDataBuffers.int(age),
                        *displayHelper.toArgsPairs().toTypedArray()
                    )
                )
            }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        displayHelper.readFromServer(args)
        ControlableBufferHelper.setPairs(this, args)
    }

    override fun onDisplay() {
        addPreTickAction {
            if (client) return@addPreTickAction
            if (conditionIndex >= conditions.size) {
                return@addPreTickAction
            }
            val (predicate, add) = conditions[conditionIndex]
            if (predicate.test(this@NetheriteWandStyle)) {
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
            val player = getPlayer() ?: let {
                remove()
                return@addPreTickAction
            }
            teleportTo(player.pos)
            if (client) {
                return@addPreTickAction
            }
            if (!player.activeItem.isOf(UsefulMagicItems.NETHERITE_WAND)) {
                displayHelper.setStatus(StatusHelper.Status.DISABLE)
                return@addPreTickAction
            }
        }
    }

    private fun getPlayer(): PlayerEntity? {
        return world!!.getPlayerByUuid(bindPlayer)
    }
}