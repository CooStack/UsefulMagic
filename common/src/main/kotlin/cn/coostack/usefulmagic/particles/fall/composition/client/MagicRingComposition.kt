package cn.coostack.usefulmagic.particles.fall.composition.client

import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.usefulmagic.particles.fall.composition.SkyFallingComposition
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class MagicRingComposition(val parent: SkyFallingComposition) :
    AutoParticleComposition(Vec3.ZERO, null) {
    var runeSize = 0.2f
    var firstRingRadius = 5.0
    var secondRingRadius = 6.0
    var secondRingYOffset = 1.0
    var runeRingYOffset = 0.5
    var rotateSpeed = PI / 64

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
                    RelativeLocation(0.0, secondRingYOffset, 0.0),
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
                .addCircle(
                    runeRingRadius,
                    (runeRingRadius * 6 * ParticleOption.getParticleCounts()).toInt()
                )
                .pointsOnEach {
                    it.y += runeRingYOffset
                }
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
