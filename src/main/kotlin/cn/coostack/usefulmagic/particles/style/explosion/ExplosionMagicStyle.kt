package cn.coostack.usefulmagic.particles.style.explosion

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ControlableParticleEffect
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI

class ExplosionMagicStyle(
    uuid: UUID = UUID.randomUUID()
) : SequencedParticleStyle(256.0, uuid) {
    override fun getParticlesCount(): Int {
        return 13
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return ExplosionMagicStyle(uuid).apply {
                this.readPacketArgs(args)
            }
        }

    }

    val random = Random(System.currentTimeMillis())

    @ControlableBuffer("age")
    var age = 0

    @ControlableBuffer("rotate_direction")
    var rotateDirection = RelativeLocation.xAxis()
    val maxAge = 240
    val scaleHelper = HelperUtil.alphaStyle(
        0.01, 1.0, 20,
//        RelativeLocation(17.0, 0.01, 0.0),
//        RelativeLocation(-3.0, -0.99, 0.0)
    )
    val statusHelper = HelperUtil.styleStatus(30)

    init {
        statusHelper.loadControler(this)
        scaleHelper.loadControler(this)
//        scaleHelper.resetScaleMax()
        scaleHelper.resetAlphaMax()
    }
    override fun remove() {
        if (statusHelper.displayStatus == 2) {
            super.remove()
        } else {
            statusHelper.setStatus(2)
        }
    }
    override fun beforeDisplay(styles: SortedMap<SortedStyleData, RelativeLocation>) {
        Math3DUtil.rotatePointsToPoint(
            styles.values.toList(), rotateDirection, axis
        )
        axis = rotateDirection
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 0

        val handler: ParticleShapeStyle.() -> ParticleShapeStyle =
            {
                this.loadScaleHelperBezierValue(
                    0.01, 1.0, 20,
                    RelativeLocation(1.0, 0.99, 0.0),
                    RelativeLocation(-19.0, 0.0, 0.0),
                ).toggleBeforeDisplay {
                    preRotateTo(it, rotateDirection)
                }
                    .toggleOnDisplay {
                        this.addPreTickAction {
                            rotateParticlesAsAxis(PI / 32)
                        }
                        reverseFunctionFromStatus(this, statusHelper)
                    }
            }
        var y = -18.01
        val added = 6.0
        res[genSingleStyle(15.0, order++, handler)] = RelativeLocation(0.0, y, 0.0)
        res[genSingleStyle(20.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(13.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(7.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(5.5, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(2.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(3.5, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(10.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(7.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(25.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(10.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(20.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        res[genSingleStyle(13.0, order++, handler)] = RelativeLocation(0.0, let { y += added;y }, 0.0)
        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).also {
            it.putAll(statusHelper.toArgsPairs())
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
    }

    override fun onDisplay() {
        addPreTickAction {
            if (age++ > maxAge) {
                statusHelper.setStatus(2)
            }
            if (statusHelper.displayStatus == 2) {
                scaleHelper.decreaseAlpha()
                toggleRelative()
            }
            if ((age - 1) % 5 == 0 && !client) {
                addSingle()
            }
        }
    }

    val option: Int
        get() = ParticleOption.getParticleCounts()

    /**
     * @param r 指的是最大圆环的半径
     */
    private fun genSingleStyle(
        r: Double,
        order: Int,
        styleHandler: ParticleShapeStyle.() -> ParticleShapeStyle
    ): SortedStyleData {
        require(r > 0)
        val subCircleRadius = r / 4
        val halfCircleRadius = r / 2
        val circleCount = (5 * option * r).toInt().coerceAtLeast(4)
        val subCircleCount = circleCount / 4
        val halfCircleCount = circleCount / 2
        return SortedStyleData({
            ParticleDisplayer.withStyle(
                styleHandler(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addWith {
                                    val res = ArrayList<RelativeLocation>()
                                    var currentTheta = 0.0
                                    repeat(8) {
                                        currentTheta += PI / 4
                                        res.addAll(
                                            PointsBuilder()
                                                .addBezierCurve(
                                                    RelativeLocation(halfCircleRadius * 2, 0.0, 0.0),
                                                    RelativeLocation(
                                                        halfCircleRadius * 0.05,
                                                        halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    RelativeLocation(
                                                        -halfCircleRadius * 0.05,
                                                        halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    halfCircleCount
                                                )
                                                .rotateAsAxis(PI / 2, RelativeLocation.xAxis())
                                                .rotateAsAxis(currentTheta)
                                                .create()
                                        )
                                    }
                                    currentTheta = PI / 24
                                    repeat(8) {
                                        currentTheta += PI / 4
                                        res.addAll(
                                            PointsBuilder()
                                                .addBezierCurve(
                                                    RelativeLocation(halfCircleRadius * 2, 0.0, 0.0),
                                                    RelativeLocation(
                                                        halfCircleRadius * 0.05,
                                                        -halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    RelativeLocation(
                                                        -halfCircleRadius * 0.05,
                                                        -halfCircleRadius * 0.9,
                                                        0.0
                                                    ),
                                                    halfCircleCount
                                                )
                                                .rotateAsAxis(PI / 2, RelativeLocation.xAxis())
                                                .rotateAsAxis(currentTheta)
                                                .create()
                                        )
                                    }
                                    res
                                }
                        ) {
                            genSingle(
                                TestEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 80, 80
                                ), 0.1f
                            )
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(subCircleRadius, subCircleCount)

                        ) {
                            genSingle(
                                TestEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 200, 200
                                ), 0.3f
                            )
                        }
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(r, circleCount)
                        ) {
                            genSingle(
                                TestEndRodEffect(UUID.randomUUID()),
                                Vec3i(
                                    255, 80, 80
                                ),
                                0.4f
                            )
                        }
                )
            )
        }, order)

    }

    private fun genSingle(effect: ControlableParticleEffect, color: Vec3i, scale: Float = 0.2f): StyleData {
        return StyleData {
            ParticleDisplayer.withSingle(effect.clone().apply { this.controlUUID = it })
        }.withParticleHandler {
            this.colorOfRGB(color.x, color.y, color.z)
            this.size = scale
        }
    }

}