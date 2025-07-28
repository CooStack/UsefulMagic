package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.StyleStatusHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import java.util.Random
import java.util.SortedMap
import java.util.UUID
import java.util.function.Predicate
import kotlin.math.PI

class AntiEntityWandStyle(
    @ControlableBuffer("target") var target: RelativeLocation,
    @ControlableBuffer("player") var bindPlayer: UUID,
    uuid: UUID = UUID.randomUUID()
) :
    SequencedParticleStyle(256.0, uuid) {

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return AntiEntityWandStyle(RelativeLocation(), UUID.randomUUID(), uuid)
                .also { it.readPacketArgs(args) }
        }
    }

    private val conditions = ArrayList<Pair<Predicate<AntiEntityWandStyle>, Int>>()

    @ControlableBuffer("condition_index")
    private var conditionIndex = 0
    val options: Int
        get() = ParticleOption.getParticleCounts()

    val scaleHelper = HelperUtil.bezierValueScaleStyle(
        1.0 / 60,
        1.0,
        60,
        RelativeLocation(40.0, 1.0 - 1 / 60.0, 0.0),
        RelativeLocation(-10.0, 0.0, 0.0),
    )

    val statusHelper = HelperUtil.styleStatus(65) as StyleStatusHelper


    @ControlableBuffer("age")
    var age = 0
    val maxAge = 360

    init {
        statusHelper.loadControler(this)
        scaleHelper.loadControler(this)
    }

    override fun getParticlesCount(): Int {
        return 12
    }

    init {
        with(conditions) {
            add(Predicate<AntiEntityWandStyle> { age > 0 } to 2)
            add(Predicate<AntiEntityWandStyle> { age > 60 } to 2)
            add(Predicate<AntiEntityWandStyle> { age > 70 } to 2)
            add(Predicate<AntiEntityWandStyle> { age > 78 } to 2)
            add(Predicate<AntiEntityWandStyle> { age > 84 } to 2)
            add(Predicate<AntiEntityWandStyle> { age > 88 } to 2)
        }
    }


    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        // 中心点
        var order = 0
        val h1 = RelativeLocation(0.0, 8.0, 0.0)
        val origin = RelativeLocation(0.0, 0.01, 0.0)
        val random = Random(System.currentTimeMillis())
        fun single(): StyleData = StyleData {
            ParticleDisplayer.withSingle(TestEndRodEffect(it))
        }.withParticleHandler {
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
            colorOfRGB(240, random.nextInt(100, 140), 255)
            size = 0.3f
        }

        fun enchant(): StyleData = StyleData {
            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
        }.withParticleHandler {
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
            colorOfRGB(240, random.nextInt(100, 140), 255)
            currentAge = random.nextInt(maxAge)
            this.size = random.nextFloat(0.2f, 1f)
        }
        // y正为下
        symmetry(res, {
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(15.0, 120 * options, 0.5)
                        ) { single() }
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(17.0, 120 * options, 0.5)
                                .addPolygonInCircle(3, 45 * options, 15.0)
                                .addPolygonInCircle(3, 45 * options, 18.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 45 * options, 15.0)
                                .addPolygonInCircle(3, 45 * options, 18.0)
                        ) { single() }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesToPoint(target)
                                rotateParticlesAsAxis(PI / 64)
                            }
                            doWithAlpha()
                        }
                )
            }, order++)
        }, h1)
        // 符文
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(30.0, 180 * options, 5.5)
                        ) { enchant() }
                        .loadScaleHelperBezierValue(
                            0.01, 1.0, 20,
                            RelativeLocation(18.0, 0.99, 0.0),
                            RelativeLocation(-1.0, 0.0, 0.0),
                        )
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesToPoint(target)
                                rotateParticlesAsAxis(-PI / 32)
                            }
                            doWithAlpha()
                        }
                )
            }, order++)
        ] = origin
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(20.0, 180 * options)
                                .addPolygonInCircle(4, 60 * options, 22.0)
                                .addPolygonInCircle(4, 60 * options, 20.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 60 * options, 22.0)
                                .addPolygonInCircle(4, 60 * options, 20.0)
                        ) { single() }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            doWithAlpha(20)
                            addPreTickAction {
                                rotateToWithAngle(target, -PI / 64)
                            }
                        }
                )
            }, order++)
        ] = origin

        symmetry(res, {
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(10.0, 120 * options, 0.7)
                        ) { single() }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            doWithAlpha(10)
                            addPreTickAction {
                                rotateToWithAngle(target, -PI / 64)
                            }
                        }
                )
            }, order++)
        }, RelativeLocation(0.0, 10.0, 0.0))

        symmetry(res, {
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(12.0, 120 * options, 1.0)
                        ) { single() }
                        .loadScaleHelper(0.1, 1.0, 10)
                        .toggleOnDisplay {
                            doWithAlpha(10)
                            addPreTickAction {
                                rotateToWithAngle(target, -PI / 64)
                            }
                        }
                )
            }, order++)
        }, RelativeLocation(0.0, 16.0, 0.0))

        res[SortedStyleData({
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addDiscreteCircleXZ(6.0, 60 * options, 0.8)
                    ) { single() }
                    .loadScaleHelper(0.1, 1.0, 10)
                    .toggleOnDisplay {
                        doWithAlpha(10)
                        addPreTickAction {
                            rotateToWithAngle(target, -PI / 64)
                        }
                    }
            )
        }, order++)] = RelativeLocation(0.0, 24.0, 0.0)

        res[SortedStyleData({
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addDiscreteCircleXZ(16.0, 60 * options, 0.8)
                    ) { single() }
                    .loadScaleHelper(0.1, 1.0, 10)
                    .toggleOnDisplay {
                        doWithAlpha(10)
                        addPreTickAction {
                            rotateToWithAngle(target, -PI / 64)
                        }
                    }
            )
        }, order++)] = RelativeLocation(0.0, -24.0, 0.0)

        res[SortedStyleData({
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(3.0, 30 * options)
                            .addPolygonInCircle(5, 10 * options, 5.0)
                            .rotateAsAxis(PI / 5)
                            .addPolygonInCircle(5, 10 * options, 5.0)
                            .addPolygonInCircle(3, 10 * options, 3.0)
                            .rotateAsAxis(PI / 3)
                            .addPolygonInCircle(3, 10 * options, 3.0)
                    ) { single() }
                    .loadScaleHelper(0.1, 1.0, 10)
                    .toggleOnDisplay {
                        doWithAlpha(10)
                        addPreTickAction {
                            rotateToWithAngle(target, -PI / 64)
                        }
                    }
            )
        }, order++)] = RelativeLocation(0.0, 34.0, 0.0)
        res[SortedStyleData({
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(22.0, 120 * options)
                            .addPolygonInCircle(5, 30 * options, 22.0)
                            .addPolygonInCircle(5, 30 * options, 24.0)
                            .rotateAsAxis(PI / 4)
                            .addPolygonInCircle(5, 30 * options, 22.0)
                            .addPolygonInCircle(5, 30 * options, 24.0)
                            .addPolygonInCircle(3, 30 * options, 20.0)
                            .addPolygonInCircle(3, 30 * options, 18.0)
                            .rotateAsAxis(PI / 3)
                            .addPolygonInCircle(3, 30 * options, 20.0)
                            .addPolygonInCircle(3, 30 * options, 18.0)
                    ) { single() }
                    .loadScaleHelper(0.1, 1.0, 10)
                    .toggleOnDisplay {
                        doWithAlpha(10)
                        addPreTickAction {
                            rotateToWithAngle(target, -PI / 64)
                        }
                    }
            )
        }, order++)] = RelativeLocation(0.0, -34.0, 0.0)
        return res
    }

    private fun symmetry(
        res: SortedMap<SortedStyleData, RelativeLocation>,
        styleData: () -> SortedStyleData,
        rel: RelativeLocation
    ) {
        res[styleData()] = rel.clone()
        res[styleData()] = rel.clone().also { it.y *= -1 }
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

    override fun onDisplay() {
        addPreTickAction {
            if (client) return@addPreTickAction
            if (conditionIndex >= conditions.size) {
                return@addPreTickAction
            }
            val (predicate, add) = conditions[conditionIndex]
            if (predicate.test(this@AntiEntityWandStyle)) {
                if (add > 0) {
                    addMultiple(add)
                } else {
                    removeMultiple(add)
                }
                conditionIndex++
            }
        }
        addPreTickAction {
            // 等待3秒
            if (age > 60 && !scaleHelper.over() && statusHelper.displayStatus != 2) {
                scaleHelper.doScale() //展开
            } else if (statusHelper.displayStatus == 2) {
                scaleHelper.doScaleReversed()
            }
            // 处理旋转
            // 修改target -> 追踪实体
            val entities = world!!.getEntitiesByClass<LivingEntity>(
                LivingEntity::class.java, Box.of(pos, 256.0, 256.0, 256.0)
            ) {
                it.uuid != bindPlayer && it.isAlive && !it.noClip
            }
            val nearest = entities.minByOrNull { it.pos.distanceTo(pos) } ?: world?.getPlayerByUuid(bindPlayer)
            ?: return@addPreTickAction
            // 取 target
            val dir = pos.relativize(nearest.pos)
            target = RelativeLocation.of(dir.normalize())
            rotateParticlesToPoint(target)
        }
        addPreTickAction {
            if (statusHelper.displayStatus != 2 && age++ > maxAge) {
                statusHelper.setStatus(StatusHelper.Status.DISABLE)
            }
        }
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

}