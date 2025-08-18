package cn.coostack.usefulmagic.particles.fall.style.client

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI

/**
 * 周边的四个带线的环
 */
class SkyFallingSub2Style(uuid: UUID, val parent: SkyFallingStyle) : SequencedParticleStyle(256.0, uuid) {
    val random = Random(System.nanoTime())
    val animateHelper =
        HelperUtil.styleSequencedAnimationHelper<SkyFallingSub2Style>()
            .loadStyle(this)
            .addAnimate({ parent.age > 1 }, 1)
            .addAnimate({ parent.age > 114 }, 1)
    var direction = RelativeLocation.yAxis()
    override fun onDisplay() {
        addPreTickAction {
            val d = parent.pos.relativize(pos)
            direction = RelativeLocation.of(d)
            rotateParticlesToPoint(direction)
        }
    }

    override fun getParticlesCount(): Int {
        return 2
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 0
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    parent.buildStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(
                                    5.0, 120 * ParticleOption.getParticleCounts()
                                )
                                .addBuilder(
                                    RelativeLocation(0.0, 0.5, 0.0),
                                    PointsBuilder()
                                        .addCircle(2.0, 30 * ParticleOption.getParticleCounts())
                                )
                                .addBuilder(
                                    RelativeLocation(0.0, 1.0, 0.0),
                                    PointsBuilder().addCircle(1.0, 20 * ParticleOption.getParticleCounts())
                                )
                                .addBuilder(
                                    RelativeLocation(0.0, 1.5, 0.0),
                                    PointsBuilder()
                                        .addCircle(1.5, 25 * ParticleOption.getParticleCounts())
                                ).addBuilder(
                                    RelativeLocation(0.0, 2.5, 0.0),
                                    PointsBuilder()
                                        .addCircle(2.0, 30 * ParticleOption.getParticleCounts())
                                )
                                .addBuilder(
                                    RelativeLocation(0.0, -8.0, 0.0), PointsBuilder()
                                        .addLine(
                                            RelativeLocation(),
                                            RelativeLocation(0.0, 32.0, 0.0),
                                            32 * 3 * ParticleOption.getParticleCounts()
                                        )
                                )
                        ) {
                            parent.buildSingleStyle().build()
                        }.appendBuilder(
                            PointsBuilder()
                                .addCircle(4.0, 15 * ParticleOption.getParticleCounts())
                        ) {
                            parent.buildSingleStyle()
                                .displayer { it ->
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }.addParticleHandler {
                                    size = 0.3f
                                    currentAge = random.nextInt(lifetime)
                                }.build()
                        }
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateToWithAngle(direction, PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    SkyFallingSubStyle(it, parent).apply {
                        r = 5.0
                        rotateSpeed = -PI / 64
                        addPreTickAction {
                            (this as SkyFallingSubStyle).direction = this@SkyFallingSub2Style.direction
                        }
                    }
                )
            }, order++)
        ] = RelativeLocation(0, 8, 0)
        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf()
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
    }
}