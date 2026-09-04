package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*

@CooAutoRegister
class EndRodSwordComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {

    @CodecField
    var color = Vec3(255.0, 255.0, 255.0)

    @CodecField
    var height: Int = 4

    @CodecField
    var lowest: Int = -1

    @CodecField
    var weight: Int = 1
    val options: Int
        get() = ParticleOption.getParticleCounts()
    val swordPoints = PointsBuilder()
        .addLine(RelativeLocation(-weight, 0, 0), RelativeLocation(weight, 0, 0), 40 * options)
        .addLine(RelativeLocation(0, lowest, 0), RelativeLocation(0, height, 0), 40 * options)


    @CodecField
    var enableAlpha = false

    @CodecField
    var direction = RelativeLocation.yAxis()

    @CodecField
    var alphaTick = 10
    val scaleHelper = CompositionScaleHelper(0.01, 1.0, 10)

    init {
        scaleHelper.loadControler(this)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return swordPoints.createWithCompositionData {
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withCParticle(
                    it,
                    if (enableAlpha) CParticleRenderLayer.TRANSLUCENT else CParticleRenderLayer.OPAQUE,
                )
            }.addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
                color = Math3DUtil.colorOf(
                    this@EndRodSwordComposition.color.x.toInt(),
                    this@EndRodSwordComposition.color.y.toInt(),
                    this@EndRodSwordComposition.color.z.toInt(),
                )
            }
        }
    }

    override fun beforeDisplay(map: Map<CompositionData, RelativeLocation>) {
        preRotateTo(map, direction)
        super.beforeDisplay(map)
    }

    override fun onDisplay() {
        addPreTickAction {
            scaleHelper.doScale()
            if (enableAlpha) {
                playCParticleAlphaTransition(
                    durationTicks = alphaTick.toFloat(),
                    alphaCurve = CParticleCurve.linear(0f, 1f),
                )
            }
            rotateToPoint(direction)
        }
    }

}
