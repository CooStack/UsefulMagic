package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.buffer.Vec3dControlerBuffer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import kotlin.math.PI
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper

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
    var enableScale = false

    @CodecField
    var scaleTick = 10

    @CodecField
    var enableAlpha = false

    @CodecField
    var direction = RelativeLocation.yAxis()

    @CodecField
    var alphaTick = 10
    val alphaHelper = CompositionAlphaHelper(0.0, 1.0, alphaTick)
    val scaleHelper = CompositionScaleHelper(0.01, 1.0, scaleTick)

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return swordPoints.createWithCompositionData {
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withSingle(
                    ControlableEndRodEffect(it)
                )
            }.addParticleInstanceInit {
                if (enableAlpha) {
                    this.textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
                }
                colorOfRGB(
                    this@EndRodSwordComposition.color.x.toInt(),
                    this@EndRodSwordComposition.color.y.toInt(),
                    this@EndRodSwordComposition.color.z.toInt(),
                )
            }
        }
    }

    override fun beforeDisplay(styles: Map<CompositionData, RelativeLocation>) {
        if (enableScale) {
            scaleHelper.loadControler(this)
        }
        if (enableAlpha) {
            alphaHelper.loadControler(this)
        }
        super.beforeDisplay(styles)
    }

    override fun onDisplay() {
        scaleHelper.scaleTick = scaleTick
        scaleHelper.recalculateStep()
        scaleHelper.resetScaleMin()
        alphaHelper.alphaTick = alphaTick
        alphaHelper.recalculateStep()
        alphaHelper.resetAlphaMin()
        addPreTickAction {
            if (enableScale) {
                scaleHelper.doScale()
            }
            if (enableAlpha) {
                alphaHelper.increaseAlpha()
            }
            rotateToPoint(direction)
        }
    }

}
