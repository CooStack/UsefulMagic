package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import java.util.Random
import java.util.SortedMap
import java.util.UUID
import kotlin.math.PI
import kotlin.math.min

/**
 * 陨石落下轨迹魔法阵
 * @param target 相对于生成位置的击落点
 */
class WandMeteoriteStyle(
    @ControlableBuffer("target")
    var target: RelativeLocation,
    uuid: UUID = UUID.randomUUID()
) : SequencedParticleStyle(256.0, uuid) {
    override fun getParticlesCount(): Int {
        return 14
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val target = args["target"]!!.loadedValue as RelativeLocation
            return WandMeteoriteStyle(target, uuid).also {
                it.readPacketArgs(args)
            }
        }
    }

    val scaleHelper = HelperUtil.bezierValueScaleStyle(
        0.01, 1.0, 20,
        RelativeLocation(1.0, 0.9, 0.0),
        RelativeLocation(-18.0, 0.0, 0.0),
    )
    val statusHelper = HelperUtil.styleStatus(50)
    val options: Int
        get() = ParticleOption.getParticleCounts()

    init {
        scaleHelper.loadControler(this)
        statusHelper.loadControler(this)
    }


    // -1代表没有任何生成
    // 用于同步
    @ControlableBuffer("sequenced_displayed_index")
    var nextIndex = -1

    @ControlableBuffer("age")
    var age = 0
    val maxAge = 180
    val random = Random(System.currentTimeMillis())
    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        // 在目标处生成光柱
        val res = sortedMapOf<SortedStyleData, RelativeLocation>()
        val direction = target.clone()
        var order = 0
        fun ParticleGroupStyle.doWithAlpha(rotate: Double, rotateTo: RelativeLocation) {
            val alphaHelper = HelperUtil.alphaStyle(0.0, 1.0, 20)
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
                rotateToWithAngle(rotateTo, rotate)
            }
        }

        fun ParticleShapeStyle.loadDefaultHelper(): ParticleShapeStyle {
            return loadScaleHelperBezierValue(
                0.1, 1.0, 10,
                RelativeLocation(1.0, 0.9, 0.0),
                RelativeLocation(-8.0, 0.0, 0.0),
            )
        }
        // 前2层魔法阵
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(18.0, 160 * options, 0.5)
                                .addDiscreteCircleXZ(24.0, 200 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 40 * options, 12.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 40 * options, 12.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(8, 30 * options, 12.0)
                                .rotateAsAxis(PI / 8)
                                .addPolygonInCircle(8, 30 * options, 12.0)
                                .addDiscreteCircleXZ(8.0, 120 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 32, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -5.0, 0.0)

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(18.0, 160 * options, 0.5)
                                .addDiscreteCircleXZ(24.0, 200 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 30 * options, 12.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 30 * options, 12.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(8, 30 * options, 12.0)
                                .rotateAsAxis(PI / 8)
                                .addPolygonInCircle(8, 30 * options, 12.0)
                                .addDiscreteCircleXZ(8.0, 120 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 32, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 5.0, 0.0)
        for (i in 0..1) {
            res[
                SortedStyleData({
                    ParticleDisplayer.withStyle(
                        ParticleShapeStyle(it)
                            .appendBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(
                                        30.0, 140 * options, 24.0
                                    )
                            ) {
                                styleEnchant().withParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    size = random.nextFloat(0.2f, 1.0f)
                                    colorOfRGB(255, 117, 148)
                                    textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                                }
                            }.loadDefaultHelper().toggleOnDisplay {
                                axis = RelativeLocation.yAxis()
                                doWithAlpha(-PI / 32, direction)
                            }
                    )
                }, order++)
            ] = RelativeLocation(0.0, if (i == 0) 20.0 else -20.0, 0.0)
        }
        // 3 个
        // Y越大越下面
        // 上面的图形 r -> 12.0 6.0 4.0
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 40 * options, 14.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 40 * options, 14.0)
                                .addDiscreteCircleXZ(14.0, 90 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -12.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(4, 20 * 6 * options, 35.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 20 * 6 * options, 35.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -16.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(20.0, 80 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -20.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(12.0, 120 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -30.0, 0.0)

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(8.0, 120 * options, 0.5)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 140 * options, 8.0 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 140 * options, 8.0 / 3.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, -35.0, 0.0)

        // 下面的图形 r -> 7.0 10.0 6.0 4.0
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(3, 10 * options, 14.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 10 * options, 14.0)
                                .addDiscreteCircleXZ(14.0, 90 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 12.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addPolygonInCircle(4, 20 * 6 * options, 30.0)
                                .rotateAsAxis(PI / 4)
                                .addPolygonInCircle(4, 20 * 6 * options, 30.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(-PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 16.0, 0.0)

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(20.0, 160 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 20.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(12.0, 120 * options, 0.5)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 30.0, 0.0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(8.0, 120 * options, 0.5)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 140 * options, 8.0 / 3.0)
                                .rotateAsAxis(PI / 3)
                                .addCycloidGraphic(2.0, 1.0, -1, 2, 140 * options, 8.0 / 3.0)
                        ) {
                            styleEndRod().withParticleHandler {
                                colorOfRGB(255, 117, 148)
                                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                            }
                        }.loadDefaultHelper().toggleOnDisplay {
                            axis = RelativeLocation.yAxis()
                            doWithAlpha(PI / 48, direction)
                        }
                )
            }, order++)
        ] = RelativeLocation(0.0, 35.0, 0.0)
        return res
    }

    fun toggle() {
        if (nextIndex == -1) {
            return
        }
        if (client) {
            addMultiple(min(getParticlesCount(), nextIndex + 1))
        }
    }


    private fun styleEndRod(): StyleData {
        return StyleData {
            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
        }
    }

    private fun styleEnchant(): StyleData {
        return StyleData {
            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
        }
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this)).also {
            it.putAll(statusHelper.toArgsPairs())
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
    }

    override fun beforeDisplay(styles: SortedMap<SortedStyleData, RelativeLocation>) {
        Math3DUtil.rotatePointsToPoint(
            styles.values.toList(), target, axis
        )
        axis = target.clone()
    }

    override fun onDisplay() {
        toggle()
        if (nextIndex == -1) {
            if (!client) {
                addMultiple(3)
            }
            nextIndex = 2
        }
        addPreTickAction {
            if (age > maxAge && statusHelper.displayStatus != 2) {
                statusHelper.setStatus(2)
            }
            if (age >= 60 && nextIndex + 1 != getParticlesCount()) {
                nextIndex = getParticlesCount() - 1
                // addAll
                if (!client) {
                    world!!.playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        10.0f,
                        1.4f
                    )
                    addMultiple(nextIndex - 2)
                }
            }
            if (age >= 60) {
                scaleHelper.doScale()
            }
            age++
            rotateParticlesToPoint(target)
        }
    }
}