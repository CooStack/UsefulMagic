package cn.coostack.usefulmagic.particles.fall.composition.client

import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.usefulmagic.particles.fall.composition.SkyFallingComposition
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class MagicRingWithMagicComposition(val parent: SkyFallingComposition) :
    AutoParticleComposition(Vec3.ZERO, null) {
    var runeSize = 0.2f
    var firstRingRadius = 50.0
    var secondRingRadius = 55.0
    var rotateSpeed = PI / 64
    var subMagicRadius = 4.0

    val scaleHelper = CompositionScaleHelper(0.01, 1.0, 10)
    val random = Random(System.nanoTime())

    init {
        scaleHelper.loadControler(this)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = HashMap<CompositionData, RelativeLocation>()
        result.putAll(
            PointsBuilder()
                .addCircle(
                    firstRingRadius,
                    (firstRingRadius * 8 * ParticleOption.getParticleCounts()).toInt()
                )
                .addBuilder(
                    RelativeLocation(),
                    PointsBuilder().addCircle(
                        secondRingRadius,
                        (secondRingRadius * 8 * ParticleOption.getParticleCounts()).toInt()
                    )
                )
                .createWithCompositionData {
                    parent.buildSingleComposition()
                }
        )

        val runeRingRadius = (secondRingRadius + firstRingRadius) / 2
        result.putAll(
            PointsBuilder()
                .addFourierSeries(
                    FourierSeriesBuilder()
                        .addFourier(1.0, 3.0)
                        .addFourier(10.0, -12.0)
                        .count((10 * runeRingRadius * ParticleOption.getParticleCounts()).toInt())
                        .scale(0.1 * runeRingRadius)
                )
                .createWithCompositionData {
                    parent.buildSingleComposition()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withCParticle(
                                it,
                                CParticleRenderLayer.TRANSLUCENT
                            )
                        }
                        .addCParticleInstanceInit {
                            effect = ControlableEnchantmentEffect(UUID.randomUUID())
                            size = runeSize
                            age = random.nextInt(0, maxAge)
                        }
                }
        )

        PointsBuilder()
            .addPolygonInCircleVertices(6, runeRingRadius)
            .create()
            .forEach { location ->
                result[
                    CompositionData().setDisplayerSupplier {
                        ParticleDisplayer.withComposition(
                            parent.buildComposition(it)
                                .applyPoint(RelativeLocation()) {
                                    CompositionData().setDisplayerSupplier {
                                        ParticleDisplayer.withComposition(
                                            SkyFallingSubComposition(parent).apply {
                                                controlUUID = it
                                                r = subMagicRadius
                                            }
                                        )
                                    }
                                }
                        )
                    }
                ] = location
            }
        return result
    }

    override fun onDisplay() {
        addPreTickAction {
            rotateAsAxis(rotateSpeed)
            if (parent.status.displayStatus == 1) {
                scaleHelper.doScale()
            }
        }
    }
}
