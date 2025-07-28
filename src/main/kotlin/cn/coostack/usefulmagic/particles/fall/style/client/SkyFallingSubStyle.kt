package cn.coostack.usefulmagic.particles.fall.style.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import java.util.Random
import java.util.UUID
import kotlin.math.PI
import kotlin.math.roundToInt

class SkyFallingSubStyle(uuid: UUID, val parent: SkyFallingStyle) : ParticleGroupStyle(256.0, uuid) {
    val random = Random(System.nanoTime())
    var rotateSpeed = PI / 64
    var r = 4.0
    var direction = RelativeLocation.yAxis()
    val scaleHelper = HelperUtil.scaleStyle(0.01, 1.0, 10)
        .apply {
            loadControler(this@SkyFallingSubStyle)
        }
    val alpha = HelperUtil.alphaStyle(0.1, 1.0, 10)
        .apply {
            loadControler(this@SkyFallingSubStyle)
        }
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val builder = PointsBuilder()
            .addFourierSeries(
                FourierPresets.circlesAndTriangles()
                    .scale(r / 12.0)
                    .count(100 * (r / 4).roundToInt() * ParticleOption.getParticleCounts())
            )
            .addPolygonInCircle(4, 15 * (r / 4).roundToInt() * ParticleOption.getParticleCounts(), r)
            .rotateAsAxis(PI / 4)
            .addPolygonInCircle(4, 15 * (r / 4).roundToInt() * ParticleOption.getParticleCounts(), r)
            .addCircle(r, 40 * (r / 4).roundToInt() * ParticleOption.getParticleCounts())
        return builder.createWithStyleData {
            parent.buildSingleStyle().build()
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (parent.status.displayStatus == 1) {
                alpha.increaseAlpha()
                scaleHelper.doScale()
            } else {
                alpha.decreaseAlpha()
            }
            rotateToWithAngle(direction, rotateSpeed)
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap()
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
    }


}