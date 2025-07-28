package cn.coostack.usefulmagic.particles.fall.style.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class MagicRingStyle(uuid: UUID, val parent: SkyFallingStyle) : ParticleGroupStyle(256.0, uuid) {
    var runeSize: Float = 0.2f
    var firstRingRadius = 5.0
    var secondRingRadius = 6.0
    var secondRingYOffset = 1.0

    var runeRingYOffset = 0.5

    var rotateSpeed = PI / 64

    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 10)
        .apply {
            loadControler(this@MagicRingStyle)
        }
    val alpha = HelperUtil.alphaStyle(0.1, 1.0, 10)
        .apply {
            loadControler(this@MagicRingStyle)
        }
    val random = Random(System.nanoTime())

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = HashMap<StyleData, RelativeLocation>()
        res.putAll(
            PointsBuilder()
                .addCircle(firstRingRadius, (firstRingRadius * 8 * ParticleOption.getParticleCounts()).toInt())
                .addBuilder(
                    RelativeLocation(0.0, secondRingYOffset, 0.0), PointsBuilder()
                        .addCircle(
                            secondRingRadius,
                            (secondRingRadius * 8 * ParticleOption.getParticleCounts()).toInt()
                        )
                )
                .createWithStyleData {
                    parent.buildSingleStyle().build()
                })

        val runeRingRadius = (secondRingRadius + firstRingRadius) / 2
        res.putAll(
            PointsBuilder()
                .addCircle(runeRingRadius, (runeRingRadius * 6 * ParticleOption.getParticleCounts()).toInt())
                .pointsOnEach {
                    it.y += runeRingYOffset
                }
                .createWithStyleData {
                    parent.buildSingleStyle()
                        .displayer {
                            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                        }
                        .addParticleHandler {
                            size = runeSize
                            currentAge = random.nextInt(0, maxAge)
                        }
                        .build()
                }
        )

        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            rotateParticlesAsAxis(rotateSpeed)
            if (parent.status.displayStatus == 1) {
                alpha.increaseAlpha()
                scaleHelper.doScale()
            } else {
                alpha.decreaseAlpha()
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf()
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {

    }
}