package cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@CooAutoRegister
class MagicRuneRingComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.517647F, 0.12549F, 0.996078F)


    @CodecField
    var radius: Double = 3.0

    @CodecField
    var radianAlphaOffset = PI / 8

    @CodecField
    var radianThetaOffset = 0.0

    @CodecField
    var alphaRotationSpeed: Double = PI / 128

    @CodecField
    var thetaRotationSpeed: Double = PI / 64


    private var direction = RelativeLocation(0.0, 1.0, 0.0)
    private var age = 0


    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        1.0,
        RelativeLocation(4.492099, 1.346079, 0.0),
        RelativeLocation(4.920993, 1.444882, 0.0)
    )

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)

    init {
        scaleHelper.loadControler(this)
        alphaHelper.loadControler(this)
        setDisabledInterval(10)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = HashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addDiscreteCircleXZ(radius, 120, 0.4)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                        }
                        .addParticleInstanceInit {
                            color = this@MagicRuneRingComposition.color
                            size = 0.8F
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
            age++
            scaleHelper.doScale()
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
            val theta = alphaRotationSpeed * age + radianAlphaOffset
            val phi = age * thetaRotationSpeed + radianThetaOffset
            direction = RelativeLocation(
                sin(theta) * cos(phi),
                cos(theta),
                sin(theta) * sin(phi)
            )
            rotateToWithAngle(direction, PI / 64)
        }
    }
}
