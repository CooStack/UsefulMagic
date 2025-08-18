package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.util.Mth
import java.util.UUID
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextInt

class EndRodExplosionStyle(uuid: UUID = UUID.randomUUID()) : ParticleGroupStyle(512.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return EndRodExplosionStyle(uuid).also {
                it.readPacketArgs(args)
            }
        }

    }

    @ControlableBuffer("ball_step")
    var ballStep = 0.2

    @ControlableBuffer("ball_radius")
    var ballRadius = 3.0

    @ControlableBuffer("max_ball_count_pow")
    var maxBallCountPow = 10

    @ControlableBuffer("min_ball_count_pow")
    var minBallCountPow = 4

    @ControlableBuffer("max_age")
    var maxAge = 120

    @ControlableBuffer("age")
    var age = 0

    @ControlableBuffer("color_r")
    var r = 255

    @ControlableBuffer("random_rotate")
    var randomRotate = true

    @ControlableBuffer("random_offset")
    var randomOffset = true

    @ControlableBuffer("random_max_age")
    var randomMaxAge = true

    @ControlableBuffer("color_g")
    var g = 255

    @ControlableBuffer("color_b")
    var b = 255

    @ControlableBuffer("particle_size")
    var particleSize = 0.2f

    @ControlableBuffer("controlable_c1")
    var c1 = RelativeLocation(1.0, 0.99, 0.0)

    @ControlableBuffer("controlable_c2")
    var c2 = RelativeLocation(-18.0, 0.0, 0.0)

    @ControlableBuffer("explosion_scale_tick")
    var explosionScaleTick = 10
    fun setExplosionBezier(c1: RelativeLocation, c2: RelativeLocation, tick: Int) {
        scaleHelper.minScale = 1.0 / tick
        scaleHelper.maxScale = 1.0
        scaleHelper.scaleTick = tick
        scaleHelper.controlPoint1 = c1
        scaleHelper.controlPoint2 = c2
        this.c1 = c1
        this.c2 = c2
        explosionScaleTick = tick
        scaleHelper.recalculateStep()
    }

    var scaleHelper = HelperUtil.bezierValueScaleStyle(
        0.01,
        1.0,
        explosionScaleTick,
        c1,
        c2,
    )


    override fun beforeDisplay(styles: Map<StyleData, RelativeLocation>) {
        setExplosionBezier(c1, c2, explosionScaleTick)
        scaleHelper.loadControler(this)
    }

    val random = Random(System.currentTimeMillis())
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = mutableMapOf<StyleData, RelativeLocation>()
        var currentStep = ballStep.coerceAtLeast(0.01)
        while (currentStep < ballRadius) {
            val progress = currentStep / ballRadius
            val currentCount =
                Mth.lerp(progress, minBallCountPow.toDouble(), maxBallCountPow.toDouble()).roundToInt()
            val builder = PointsBuilder().addBall(
                currentStep, currentCount
            )

            if (randomRotate) {
                builder.rotateAsAxis(random.nextDouble(-PI, PI))
            }

            if (randomOffset) {
                builder.pointsOnEach {
                    it.x += random.nextDouble(-sqrt(currentStep), sqrt(currentStep))
                    it.y += random.nextDouble(-sqrt(currentStep), sqrt(currentStep))
                    it.z += random.nextDouble(-sqrt(currentStep), sqrt(currentStep))
                }
            }

            res.putAll(
                builder.createWithStyleData {
                    StyleData {
                        ParticleDisplayer.withSingle(
                            ControlableEndRodEffect(it)
                        )
                    }.withParticleHandler {
                        colorOfRGB(
                            this@EndRodExplosionStyle.r,
                            this@EndRodExplosionStyle.g,
                            this@EndRodExplosionStyle.b
                        )
                        this.size = particleSize
                        if (randomMaxAge) {
                            this.lifetime = max(20, this@EndRodExplosionStyle.maxAge - random.nextInt(60)) - 10
                        } else {
                            this.lifetime = this@EndRodExplosionStyle.maxAge
                        }
                        textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                    }.withParticleControlerHandler {
                        addPreTickAction {
                            this.currentAge++
                            if (currentAge >= maxAge - 15) {
                                particleAlpha *= 0.9f
                            }
                        }
                    }
                }
            )
            currentStep += ballStep
        }
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            scaleHelper.doScale()
            toggleRelative()
            if (age++ > maxAge) {
                remove()
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return ControlableBufferHelper.getPairs(this)
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
    }
}