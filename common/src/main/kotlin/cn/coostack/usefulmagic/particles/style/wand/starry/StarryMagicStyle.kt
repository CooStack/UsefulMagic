package cn.coostack.usefulmagic.particles.style.wand.starry

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI
import kotlin.random.Random

/**
 * 释放时的法杖 (朝向在释放时锁定)
 */
class StarryMagicStyle(uuid: UUID = UUID.randomUUID()) :
    SequencedParticleStyle(256.0, uuid) {
    val status = HelperUtil.styleStatus(20)
    val scaleHelper = HelperUtil.bezierValueScaleStyle(
        0.01, 1.0, 20,
        RelativeLocation(1.0, 0.9, 0.0),
        RelativeLocation(-18.0, 0.0, 0.0),
    )

    @ControlableBuffer("direction")
    var direction = Vec3.ZERO

    @ControlableBuffer("age")
    var age = 0

    val maxAge = 60 + 30 * 20

    @ControlableBuffer("player")
    var player: UUID = UUID.randomUUID()

    init {
        HelperUtil.styleSequencedAnimationHelper<StarryMagicStyle>()
            .loadStyle(this)
            .addAnimate({ this.age > 1 }, 1)
            .addAnimate({ this.age > 60 }, 6)
        status.loadControler(this)
        scaleHelper.loadControler(this)
        autoToggle = true
    }

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return StarryMagicStyle(uuid)
        }
    }

    val random = Random(System.currentTimeMillis())
    override fun getParticlesCount(): Int {
        return 7
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 0
        // 第一层
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendPoint(RelativeLocation()) {
                            // 内圈小圆
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(15.0, 240 * ParticleOption.getParticleCounts())
                                                .addFourierSeries(
                                                    FourierSeriesBuilder()
                                                        .addFourier(1.0, 2.0)
                                                        .addFourier(2.0, -1.0)
                                                        .count(240 * ParticleOption.getParticleCounts())
                                                        .scale(5.0)
                                                )
                                                .rotateAsAxis(PI / 3)
                                                .addFourierSeries(
                                                    FourierSeriesBuilder()
                                                        .addFourier(1.0, 2.0)
                                                        .addFourier(2.0, -1.0)
                                                        .count(240 * ParticleOption.getParticleCounts())
                                                        .scale(5.0)
                                                )
                                        ) { rel ->
                                            getSingle(rel).build()
                                        }
                                        .loadScaleHelper(0.01, 1.0, 10)
                                        .toggleOnDisplay {
                                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                                            alpha.loadControler(this)
                                            addPreTickAction {
                                                if (status.displayStatus == 1) {
                                                    alpha.increaseAlpha()
                                                } else {
                                                    alpha.decreaseAlpha()
                                                }
                                            }
                                            this.addPreTickAction {
                                                rotateToWithAngle(RelativeLocation.of(direction), PI / 32)
                                            }
                                        }
                                )
                            }
                        }.appendPoint(RelativeLocation()) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(25.0, 240 * ParticleOption.getParticleCounts())
                                                .addPolygonInCircle(4, 40 * ParticleOption.getParticleCounts(), 25.0)
                                                .addPolygonInCircle(4, 40 * ParticleOption.getParticleCounts(), 27.0)
                                                .rotateAsAxis(PI / 4)
                                                .addPolygonInCircle(4, 40 * ParticleOption.getParticleCounts(), 27.0)
                                                .addPolygonInCircle(4, 40 * ParticleOption.getParticleCounts(), 25.0)
                                        ) { rel ->
                                            getSingle(rel).build()
                                        }.loadScaleHelper(0.01, 1.0, 10)
                                        .toggleOnDisplay {
                                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                                            alpha.loadControler(this)
                                            addPreTickAction {
                                                if (status.displayStatus == 1) {
                                                    alpha.increaseAlpha()
                                                } else {
                                                    alpha.decreaseAlpha()
                                                }
                                            }
                                            this.addPreTickAction {
                                                rotateToWithAngle(RelativeLocation.of(direction), -PI / 48)
                                            }
                                        }
                                )
                            }
                        }.appendPoint(RelativeLocation()) {
                            StyleData {
                                ParticleDisplayer.withStyle(
                                    ParticleShapeStyle(it)
                                        .appendBuilder(
                                            PointsBuilder()
                                                .addCircle(32.0, 240 * ParticleOption.getParticleCounts())
                                                .addCircle(30.0, 240 * ParticleOption.getParticleCounts())
                                        ) { rel ->
                                            getSingle(rel)
                                                .addParticleHandler {
                                                    this.size = 0.4f
                                                }
                                                .build()
                                        }.appendBuilder(
                                            PointsBuilder()
                                                .addCircle(31.0, 120)
                                        ) { rel ->
                                            getSingle(rel)
                                                .displayer {
                                                    ParticleDisplayer.withSingle(
                                                        ControlableEnchantmentEffect(it)
                                                    )
                                                }
                                                .addParticleHandler {
                                                    this.currentAge = random.nextInt(this.lifetime)
                                                    this.size = 0.6f
                                                }
                                                .build()
                                        }
                                        .loadScaleHelper(0.01, 1.0, 10)
                                        .toggleOnDisplay {
                                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                                            alpha.loadControler(this)
                                            addPreTickAction {
                                                if (status.displayStatus == 1) {
                                                    alpha.increaseAlpha()
                                                } else {
                                                    alpha.decreaseAlpha()
                                                }
                                            }
                                            this.addPreTickAction {
                                                rotateToWithAngle(RelativeLocation.of(direction), PI / 92)
                                            }
                                        }
                                )
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, -10, 0)

        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(40.0, 240 * ParticleOption.getParticleCounts())
                                .addCircle(38.0, 240 * ParticleOption.getParticleCounts())
                                .addPolygonInCircle(5, 120 * ParticleOption.getParticleCounts(), 38.0)
                                .rotateAsAxis(PI / 5)
                                .addPolygonInCircle(5, 120 * ParticleOption.getParticleCounts(), 38.0)
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(39.0, 120)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.6f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, 15, 0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(20.0, 240 * ParticleOption.getParticleCounts())
                                .addCircle(18.0, 240 * ParticleOption.getParticleCounts())
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(19.0, 120)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.6f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, 30, 0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(25.0, 120 * ParticleOption.getParticleCounts())
                                .addCircle(23.0, 120 * ParticleOption.getParticleCounts())
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(22.5, 120)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.6f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), -PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, 40, 0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addDiscreteCircleXZ(6.0, 60 * ParticleOption.getParticleCounts(), 0.8)
                                .addFourierSeries(
                                    FourierSeriesBuilder()
                                        .addFourier(1.0, 2.0)
                                        .addFourier(2.0, -1.0)
                                        .count(120 * ParticleOption.getParticleCounts())
                                        .scale(2.0)
                                )
                                .rotateAsAxis(PI / 3)
                                .addFourierSeries(
                                    FourierSeriesBuilder()
                                        .addFourier(1.0, 2.0)
                                        .addFourier(2.0, -1.0)
                                        .count(120 * ParticleOption.getParticleCounts())
                                        .scale(2.0)
                                )
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), PI / 48)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, 55, 0)
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(38.0, 210 * ParticleOption.getParticleCounts())
                                .addCircle(35.0, 210 * ParticleOption.getParticleCounts())
                                .addPolygonInCircle(6, 50 * ParticleOption.getParticleCounts(), 35.0)
                                .rotateAsAxis(PI / 6)
                                .addPolygonInCircle(6, 50 * ParticleOption.getParticleCounts(), 35.0)
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(37.0, 210)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.8f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), -PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, -20, 0)

        // 后面的部分
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(20.0, 210 * ParticleOption.getParticleCounts())
                                .addCircle(18.0, 210 * ParticleOption.getParticleCounts())
                                .addPolygonInCircle(3, 50 * ParticleOption.getParticleCounts(), 18.0)
                                .addPolygonInCircle(3, 50 * ParticleOption.getParticleCounts(), 16.0)
                                .rotateAsAxis(PI / 3)
                                .addPolygonInCircle(3, 50 * ParticleOption.getParticleCounts(), 16.0)
                                .addPolygonInCircle(3, 50 * ParticleOption.getParticleCounts(), 18.0)
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.4f
                                }
                                .build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(19.0, 210)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.6f
                                }
                                .build()
                        }
                        .loadScaleHelper(0.01, 1.0, 10)
                        .toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.01, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), -PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation(0, -40, 0)

        return res
    }

    private fun getSingle(it: RelativeLocation): StyleDataBuilder {
        return StyleDataBuilder()
            .addParticleHandler {
                val start = Vector3f(1f, 0f, 0f)
                color = start
                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
            }.addParticleControlerHandler {
                this.addPreTickAction {
                    val final = Vector3f(1f, 120 / 255f, 100 / 255f)
                    val start = Vector3f(1f, 0f, 0f)
                    val length = this.loc.distanceTo(this@StarryMagicStyle.pos)
                    val current = GraphMathHelper.lerp(length / 32f, start, final)
                    this.color = current
                }
            }
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap<String, ParticleControlerDataBuffer<*>>().apply {
            putAll(status.toArgsPairs())
            putAll(ControlableBufferHelper.getPairs(this@StarryMagicStyle))
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }

    override fun onDisplay() {
        addPreTickAction {
            age++
            if (age > 70) {
                scaleHelper.doScale()
            }
            if (age == 60) {
                world!!.playSound(
                    null, pos.x, pos.y, pos.z, UsefulMagicSoundEvents.STAR.get(), SoundSource.PLAYERS, 5f, 1f
                )
            }
            rotateParticlesToPoint(RelativeLocation.of(direction))
            if (age > maxAge) status.setStatus(StatusHelper.Status.DISABLE)
        }
    }

}