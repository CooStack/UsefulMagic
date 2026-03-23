package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
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
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

@CooAutoRegister
class EndRodExplosionComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {

    @CodecField
    var ballStep = 0.2

    @CodecField
    var ballRadius = 3.0

    @CodecField
    var maxBallCountPow = 10

    @CodecField
    var minBallCountPow = 4

    @CodecField
    var maxAge = 120

    @CodecField
    var age = 0

    @CodecField
    var r = 255

    @CodecField
    var randomRotate = true

    @CodecField
    var randomOffset = true

    @CodecField
    var randomMaxAge = true

    @CodecField
    var g = 255

    @CodecField
    var b = 255

    @CodecField
    var particleSize = 0.2f

    @CodecField
    var c1 = RelativeLocation(1.0, 0.99, 0.0)

    @CodecField
    var c2 = RelativeLocation(-18.0, 0.0, 0.0)

    @CodecField
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

    var scaleHelper = CompositionBezierScaleHelper(
        explosionScaleTick,
        0.01,
        1.0,
        c1,
        c2,
    )

    override fun beforeDisplay(styles: Map<CompositionData, RelativeLocation>) {
        setExplosionBezier(c1, c2, explosionScaleTick)
        scaleHelper.loadControler(this)
    }

    val random = Random(System.currentTimeMillis())
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val res = mutableMapOf<CompositionData, RelativeLocation>()
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
                builder.createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                        ParticleDisplayer.withSingle(
                            ControlableEndRodEffect(it)
                        )
                    }.addParticleInstanceInit {
                        colorOfRGB(
                            this@EndRodExplosionComposition.r,
                            this@EndRodExplosionComposition.g,
                            this@EndRodExplosionComposition.b
                        )
                        this.size = particleSize
                        if (randomMaxAge) {
                            this.lifetime = max(20, this@EndRodExplosionComposition.maxAge - random.nextInt(60)) - 10
                        } else {
                            this.lifetime = this@EndRodExplosionComposition.maxAge
                        }
                        textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                    }.addParticleControlerInstanceInit {
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

}

