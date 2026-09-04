package cn.coostack.usefulmagic.particles.composition
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random
import java.util.UUID

@CooAutoRegister
class GoldenBallBarrageComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var pColor: Vec3 = Vec3(255.0, 255.0, 255.0)

    @CodecField
    var size: Float = 0.3f

    @CodecField
    var r: Double = 2.0

    @CodecField
    var countPow: Int = 16

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return mapOf(
            createEnchantRingCompositionData() to RelativeLocation(),
            createEndRodBallCompositionData() to RelativeLocation(),
            createRotatingCircleCompositionData(RelativeLocation(-1.0, 0.0, -1.0)) to RelativeLocation(),
            createRotatingCircleCompositionData(RelativeLocation(1.0, -1.0, 1.0)) to RelativeLocation()
        )
    }

    private fun createEnchantRingCompositionData(): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(it)
                    .applyBuilder(PointsBuilder().addDiscreteCircleXZ(4.0, 80 * options, 2.5)) {
                        withEffectEnchant()
                    }
                    .loadScaleHelper(1 / 40.0, 1.0, 40)
                    .applyDisplayAction {
                        addPreTickAction {
                            rotateAsAxis(-PI / 18)
                        }
                    }
            )
        }
    }

    private fun createEndRodBallCompositionData(): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(it)
                    .applyBuilder(PointsBuilder().addBall(r, countPow * options / 2)) {
                        withEffectEndRod()
                    }
                    .loadScaleHelper(1 / 40.0, 1.0, 40)
                    .applyDisplayAction {
                        addPreTickAction {
                            rotateAsAxis(PI / 36)
                        }
                    }
            )
        }
    }

    private fun createRotatingCircleCompositionData(rotateTo: RelativeLocation): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                ParticleShapeComposition(it)
                    .applyBuilder(PointsBuilder().addCircle(4.0, countPow * countPow * options)) {
                        withEffectEndRod()
                    }
                    .loadScaleHelper(1 / 40.0, 1.0, 40)
                    .applyDisplayAction {
                        axis = RelativeLocation(1.0, 0.0, 0.0)
                        rotateToPoint(rotateTo)
                        addPreTickAction {
                            rotateAsAxis(PI / 36)
                        }
                    }
            )
        }
    }

    private fun withEffectEnchant(): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
        }.addCParticleInstanceInit {
            effect = ControlableEnchantmentEffect(UUID.randomUUID())
            color = Math3DUtil.colorOf(255, 100, 20)
            alpha = 0.4f
            this.size = 0.3f
            age = Random.nextInt(0, maxAge)
        }.addCParticleControlerInstanceInit {
            var currentAlpha = 0.4f
            var tick = true
            addPreTickAction {
                if (currentAlpha >= 0.9f) {
                    tick = false
                }
                if (currentAlpha <= 0.2f) {
                    tick = true
                }
                currentAlpha += if (tick) 0.05f else -0.05f
                setAlpha(currentAlpha)
            }
        }
    }

    private fun withEffectEndRod(): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
        }.addCParticleInstanceInit {
            effect = ControlableEndRodEffect(UUID.randomUUID())
            color = Math3DUtil.colorOf(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
            alpha = 0.4f
            this.size = this@GoldenBallBarrageComposition.size
            age = Random.nextInt(0, maxAge)
        }.addCParticleControlerInstanceInit {
            var currentAlpha = 0.4f
            var tick = true
            addPreTickAction {
                if (currentAlpha >= 0.9f) {
                    tick = false
                }
                if (currentAlpha <= 0.2f) {
                    tick = true
                }
                currentAlpha += if (tick) 0.05f else -0.05f
                setAlpha(currentAlpha)
            }
        }
    }

    override fun onDisplay() {
    }
}







