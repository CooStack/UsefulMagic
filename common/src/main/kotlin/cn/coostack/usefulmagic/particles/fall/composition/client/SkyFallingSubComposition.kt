package cn.coostack.usefulmagic.particles.fall.composition.client

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.particles.fall.composition.SkyFallingComposition
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.roundToInt

class SkyFallingSubComposition(val parent: SkyFallingComposition) :
    AutoParticleComposition(Vec3.ZERO, null) {
    var rotateSpeed = PI / 64
    var r = 4.0
    var direction = RelativeLocation.yAxis()

    val scaleHelper = CompositionScaleHelper(0.01, 1.0, 10)

    init {
        scaleHelper.loadControler(this)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addFourierSeries(
                FourierPresets.circlesAndTriangles()
                    .scale(r / 12.0)
                    .count(100 * (r / 4).roundToInt() * ParticleOption.getParticleCounts())
            )
            .addPolygonInCircle(
                4,
                15 * (r / 4).roundToInt() * ParticleOption.getParticleCounts(),
                r
            )
            .rotateAsAxis(PI / 4)
            .addPolygonInCircle(
                4,
                15 * (r / 4).roundToInt() * ParticleOption.getParticleCounts(),
                r
            )
            .addCircle(
                r,
                40 * (r / 4).roundToInt() * ParticleOption.getParticleCounts()
            )
            .createWithCompositionData {
                parent.buildSingleComposition()
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (parent.status.displayStatus == 1) {
                scaleHelper.doScale()
            }
            rotateToWithAngle(direction, rotateSpeed)
        }
    }
}
