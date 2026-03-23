package cn.coostack.usefulmagic.particles.entity.dragon.composition.skills

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class DragonLaserSmallComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var direction: RelativeLocation = RelativeLocation(0.0, 0.0, 0.0)

    @CodecField
    var color: Vector3f = Vector3f(0.615686F, 0.360784F, 1F)

    private val scaleHelper = CompositionBezierScaleHelper(10, 0.01, 1.0, RelativeLocation(5.609481, 1.082393, 0.0), RelativeLocation(1.670429, 1.250484, 0.0))

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)
    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
        setDisabledInterval(10)
    }
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addCircle(4.0, 120)
                .addCircle(3.0, 120)
                .addPolygonInCircle(4, 30, 3.0)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addPolygonInCircle(4, 30, 3.0)
                        .rotateAsAxis(-0.25*PI, RelativeLocation(0.0, 1.0, 0.0))
                )
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                        .addParticleInstanceInit {
                            size = 0.3F
                            color = this@DragonLaserSmallComposition.color
                            textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                        }
                }
        )

        result.putAll(
            PointsBuilder()
                .addCircle(3.5, 40)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                        }
                        .addParticleInstanceInit {
                            size = 0.5F
                            color = this@DragonLaserSmallComposition.color
                            currentAge = Random.nextInt(lifetime)
                            textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                        }
                }
        )

        return result
    }
    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }
    override fun onDisplay() {
        addPreTickAction {
            scaleHelper.doScale()
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
            rotateToWithAngle(direction, 0.031831*PI)
        }
    }
}