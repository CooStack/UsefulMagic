package cn.coostack.usefulmagic.particles.fall.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.particles.fall.style.client.MagicRingStyle
import cn.coostack.usefulmagic.particles.fall.style.client.MagicRingWithMagicStyle
import cn.coostack.usefulmagic.particles.fall.style.client.SkyFallingSub2Style
import cn.coostack.usefulmagic.particles.fall.style.client.SkyFallingSubStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.sound.SoundCategory
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI
import kotlin.math.roundToInt

/**
 * 超位魔法 - 天空坠落 主实现
 *
 * 由于行数可能比较多 - 因此分成其他类用于区分不同的动画
 * 主层嵌套其他层
 */
class SkyFallingStyle(
    uuid: UUID = UUID.randomUUID()
) :
    SequencedParticleStyle(256.0, uuid) {
    @ControlableBuffer("bind_player")
    var bindPlayer: UUID = UUID.randomUUID()
    val status = HelperUtil.styleStatus(15).apply {
        loadControler(this@SkyFallingStyle)
    }

    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.5, 30).apply {
        loadControler(this@SkyFallingStyle)
    }

    val animationHelper = HelperUtil.styleSequencedAnimationHelper<SkyFallingStyle>()
        .loadStyle(this)
        .addAnimate({
            age > 1
        }, 8)
        .addAnimate({
            age > 100
        }, 1)
        .addAnimate({
            age > 102
        }, 1)
        .addAnimate({
            age > 104
        }, 1)
        .addAnimate({
            age > 106
        }, 1)
        .addAnimate({
            age > 108
        }, 1)
        .addAnimate({
            age > 110
        }, 1)
        .addAnimate({
            age > 112
        }, 1)
        .addAnimate({
            age > 114
        }, 1)
        .addAnimate({
            age > 116
        }, 1)
        .addAnimate({
            age > 118
        }, 1)
        .addAnimate({
            age > 120
        }, 1)

    val random = Random(System.currentTimeMillis())
    override fun getParticlesCount(): Int {
        return 19
    }

    @ControlableBuffer("age")
    var age = 0

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return SkyFallingStyle(uuid)
        }
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        val footStyle = SortedStyleDataBuilder()
            .displayer {
                ParticleDisplayer.withStyle(
                    buildStyle(it)
                        .appendPoint(RelativeLocation()) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    buildStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addFourierSeries(
                                                    FourierPresets.circlesAndTriangles()
                                                        .scale(0.3)
                                                        .count(180 * ParticleOption.getParticleCounts())
                                                )
                                                .addPolygonInCircle(4, 15 * ParticleOption.getParticleCounts(), 4.0)
                                                .rotateAsAxis(PI / 4)
                                                .addPolygonInCircle(4, 15 * ParticleOption.getParticleCounts(), 4.0)
                                        ) {
                                            buildSingleStyle().build()
                                        }.toggleOnDisplay {
                                            addPreTickAction {
                                                rotateParticlesAsAxis(-PI / 128)
                                            }
                                        }
                                )
                            }
                        }.appendPoint(RelativeLocation()) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    buildStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(
                                                    5.0,
                                                    30 * ParticleOption.getParticleCounts()
                                                )
                                        ) {
                                            buildSingleStyle()
                                                .displayer {
                                                    ParticleDisplayer.withSingle(
                                                        ControlableEnchantmentEffect(it)
                                                    )
                                                }.addParticleHandler {
                                                    size = 0.3f
                                                    this.currentAge = random.nextInt(0, maxAge)
                                                }
                                                .build()
                                        }.toggleOnDisplay {
                                            addPreTickAction {
                                                rotateParticlesAsAxis(PI / 64)
                                            }
                                        }
                                )
                            }
                        }.appendPoint(RelativeLocation()) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    buildStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(4.0, 90 * ParticleOption.getParticleCounts())
                                                .addCircle(6.0, 120 * ParticleOption.getParticleCounts())
                                        ) {
                                            buildSingleStyle().build()
                                        }.toggleOnDisplay {
                                            addPreTickAction {
                                                rotateParticlesAsAxis(PI / 64)
                                            }
                                        }
                                )
                            }
                        }
                )
            }
        var currentOrder = 0
        res[footStyle.build(currentOrder++)] = RelativeLocation()
        res[SortedStyleDataBuilder().displayer {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(it).appendBuilder(
                    PointsBuilder()
                        .addPolygonInCircleVertices(4, 13.0)
                        .pointsOnEach { it -> it.y += 9 }
                ) { rel ->
                    StyleData { it ->
                        ParticleDisplayer.withStyle(
                            SkyFallingSub2Style(it, this)
                                .apply {
                                    direction = rel
                                }
                        )
                    }
                }.toggleOnDisplay {
                    addPreTickAction {
                        rotateParticlesAsAxis(PI / 128)
                    }
                }
            )
        }.build(currentOrder++)] = RelativeLocation()
        res[
            buildRingStyle(9.5, 10.0, 0.25, 0.5)
                .build(currentOrder++)
        ] = RelativeLocation(0, 1, 0)
        res[
            buildRingStyle(14.0, 14.0, 0.5, 1.0)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 5.0, 0.0)
        res[
            buildRingStyle(6.0, 6.0, 0.5, 1.0)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 6.0, 0.0) // 1

        res[
            buildRingStyle(11.0, 9.0, 0.25, 0.5, 0.3f)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 10.0, 0.0)
        res[
            buildRingStyle(6.5, 7.0, 0.0, 0.0, 0.1f, PI / 32)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 12.0, 0.0) //2
        res[buildSubStyle(3.0).build(currentOrder++)] = RelativeLocation(0.0, 13.0, 0.0) // 3

        // 阶段 2 - 准备释放

        res[
            buildRingStyle(28.0, 30.0, 0.0, 0.0, 0.4f, -PI / 128)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 4.0, 0.0)
        res[buildSubStyle(8.0).build(currentOrder++)] = RelativeLocation(0.0, 15.0, 0.0)
        res[
            buildRingStyle(17.0, 19.0, 0.0, 0.0, 0.4f)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 18.0, 0.0)
        res[
            buildRingStyle(12.0, 14.0, 0.0, 0.0, 0.3f)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 16.0, 0.0)
        res[
            buildRingStyle(22.0, 24.0, 0.0, 0.0, 0.4f, PI / 64)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 14.0, 0.0)
        res[
            buildRingStyle(36.0, 38.5, 0.0, 0.0, 0.2f, PI / 128)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 21.0, 0.0)
        res[
            buildCircleStyle(34.0)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 19.0, 0.0)

        res[
            buildRingStyle(18.0, 20.0, 0.0, 0.0, 0.3f, PI / 128)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 23.0, 0.0)

        res[
            buildRingWithMagicStyle(44.0, 54.0, 5.0, 0.5f, -PI / 128)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 14.0, 0.0)

        res[buildSubStyle(10.0).build(currentOrder++)] = RelativeLocation(0.0, 25.0, 0.0)

        res[
            buildRingWithMagicStyle(16.0, 24.0, 4.0, 0.3f, -PI / 128)
                .build(currentOrder++)
        ] = RelativeLocation(0.0, 30.0, 0.0)

        return res
    }

    fun buildCircleStyle(r: Double): SortedStyleDataBuilder {
        return SortedStyleDataBuilder()
            .displayer {
                ParticleDisplayer.withStyle(
                    buildStyle(it)
                        .appendBuilder(
                            PointsBuilder().addCircle(r, (r * 15 * ParticleOption.getParticleCounts()).roundToInt())
                        ) {
                            buildSingleStyle().build()
                        }.toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 128)
                            }
                        }
                )
            }
    }

    fun buildSubStyle(r: Double, rotateSpeed: Double = PI / 64): SortedStyleDataBuilder {
        return SortedStyleDataBuilder()
            .displayer {
                ParticleDisplayer.withStyle(
                    SkyFallingSubStyle(it, this)
                        .apply {
                            this.rotateSpeed = rotateSpeed
                            this.r = r
                        }
                )
            }
    }

    fun buildRingWithMagicStyle(
        r1: Double,
        r2: Double,
        subMagicRadius: Double,
        runeSize: Float = 0.2f,
        rotateSpeed: Double = PI / 64,
    ): SortedStyleDataBuilder {
        return SortedStyleDataBuilder()
            .displayer {
                ParticleDisplayer.withStyle(
                    MagicRingWithMagicStyle(it, this)
                        .apply {
                            this.firstRingRadius = r1
                            this.secondRingRadius = r2
                            this.subMagicRadius = subMagicRadius
                            this.runeSize = runeSize
                            this.rotateSpeed = rotateSpeed
                        }
                )
            }
    }


    fun buildRingStyle(
        r1: Double,
        r2: Double,
        runeOffset: Double,
        r2Offset: Double,
        runeSize: Float = 0.2f,
        rotateSpeed: Double = PI / 64,
    ): SortedStyleDataBuilder {
        return SortedStyleDataBuilder()
            .displayer {
                ParticleDisplayer.withStyle(
                    MagicRingStyle(it, this)
                        .apply {
                            this.firstRingRadius = r1
                            this.secondRingYOffset = r2Offset
                            this.runeRingYOffset = runeOffset
                            this.secondRingRadius = r2
                            this.runeSize = runeSize
                            this.rotateSpeed = rotateSpeed
                        }
                )
            }
    }

    fun buildSingleStyle(): StyleDataBuilder {
        val builder = StyleDataBuilder()
        builder.addParticleHandler {
            colorOfRGB(100, 200, 255)
            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
        }
        return builder
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(
            ControlableBufferHelper.getPairs(this)
        ).apply {
        }
    }

    fun buildStyle(uuid: UUID, scaleTick: Int = 10): ParticleShapeStyle {
        val style = ParticleShapeStyle(uuid)
            .loadScaleHelper(0.01, 1.0, scaleTick)
            .toggleOnDisplay {
                val alpha = HelperUtil.alphaStyle(0.1, 1.0, scaleTick)
                alpha.loadControler(this)
                addPreTickAction {
                    if (status.displayStatus == 1) {
                        scaleHelper?.doScale()
                        alpha.increaseAlpha()
                    } else {
                        alpha.decreaseAlpha()
                    }
                }
            }
        return style
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
    }

    override fun onDisplay() {
        autoToggle = true  // 开启后 第二阶段会生成多余的粒子  原因未知
        addPreTickAction {
            if (age % 80 == 0) {
                world!!.playSound(
                    null, pos.x, pos.y, pos.z,
                    UsefulMagicSoundEvents.SKY_FALLING_MAGIC_IDLE,
                    SoundCategory.PLAYERS,
                    10f, 1f
                )
            }
            age++
//            change(
//                { age++ }, mapOf(
//                    "age" to ParticleControlerDataBuffers.int(age + 1)
//                )
//            )
            if (age > 12 * 20) {
                status.setStatus(2)
            }
            if (status.displayStatus == 1 && scale <= 1.0) {
                scaleHelper.doScale()
            }
            if (age > 100 && status.displayStatus == 1) {
                scaleHelper.doScale()
                if (age % 2 == 0 && age < 120) {
                    world!!.playSound(
                        null, pos.x, pos.y, pos.z,
                        UsefulMagicSoundEvents.MAGIC_ACTIVATE,
                        SoundCategory.PLAYERS,
                        10f, 1f
                    )
                }
            }

            val player = world?.getPlayerByUuid(bindPlayer) ?: return@addPreTickAction
            teleportTo(player.pos)
        }
    }
}