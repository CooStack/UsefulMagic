package cn.coostack.usefulmagic.particles.fall.composition.client

import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.fall.composition.SkyFallingComposition
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI

class SkyFallingSub2Composition(val parent: SkyFallingComposition) :
    AutoSequencedParticleComposition(Vec3.ZERO, null) {
    val random = Random(System.nanoTime())
    var direction = RelativeLocation.yAxis()

    init {
        animate
            .addAnimate(1) { parent.age > 1 }
            .addAnimate(1) { parent.age > 114 }
            .clientOnly()
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val result = TreeMap<CompositionData, RelativeLocation>()
        var order = 0
        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        parent.buildComposition(it)
                            .applyBuilder(
                                PointsBuilder()
                                    .addCircle(5.0, 120 * ParticleOption.getParticleCounts())
                                    .addBuilder(
                                        RelativeLocation(0.0, 0.5, 0.0),
                                        PointsBuilder().addCircle(
                                            2.0,
                                            30 * ParticleOption.getParticleCounts()
                                        )
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 1.0, 0.0),
                                        PointsBuilder().addCircle(
                                            1.0,
                                            20 * ParticleOption.getParticleCounts()
                                        )
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 1.5, 0.0),
                                        PointsBuilder().addCircle(
                                            1.5,
                                            25 * ParticleOption.getParticleCounts()
                                        )
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, 2.5, 0.0),
                                        PointsBuilder().addCircle(
                                            2.0,
                                            30 * ParticleOption.getParticleCounts()
                                        )
                                    )
                                    .addBuilder(
                                        RelativeLocation(0.0, -8.0, 0.0),
                                        PointsBuilder().addLine(
                                            RelativeLocation(),
                                            RelativeLocation(0.0, 128.0, 0.0),
                                            32 * 3 * ParticleOption.getParticleCounts()
                                        )
                                    )
                            ) {
                                parent.buildSingleComposition()
                            }
                            .applyBuilder(
                                PointsBuilder().addCircle(
                                    4.0,
                                    15 * ParticleOption.getParticleCounts()
                                )
                            ) {
                                parent.buildSingleComposition()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(
                                            it,
                                            CParticleRenderLayer.TRANSLUCENT
                                        )
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                        size = 0.3f
                                        age = random.nextInt(maxAge)
                                    }
                            }
                            .applyDisplayAction {
                                addPreTickAction {
                                    rotateToWithAngle(direction, PI / 64)
                                }
                            }
                    )
                }
                .apply { this.order = order++ }
        ] = RelativeLocation()
        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        SkyFallingSubComposition(parent).apply {
                            controlUUID = it
                            r = 5.0
                            rotateSpeed = -PI / 64
                            addPreTickAction {
                                (this as SkyFallingSubComposition).direction =
                                    this@SkyFallingSub2Composition.direction
                            }
                        }
                    )
                }
                .apply { this.order = order++ }
        ] = RelativeLocation(0, 8, 0)
        return result
    }

    override fun onDisplay() {
        addPreTickAction {
            val relative = parent.position.relativize(position)
            direction = RelativeLocation.of(relative)
            rotateToPoint(direction)
        }
    }
}
