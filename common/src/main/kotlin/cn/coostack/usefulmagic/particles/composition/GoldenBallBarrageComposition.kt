package cn.coostack.usefulmagic.particles.composition
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random

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
            ParticleDisplayer.withSingle(
                ControlableEnchantmentEffect(it)
            )
        }.addParticleInstanceInit {
            colorOfRGB(255, 100, 20)
            particleAlpha = 0.4f
            this.size = 0.3f
            currentAge = Random.nextInt(0, lifetime)
            textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
        }.addParticleControlerInstanceInit {
            var tick = true
            addPreTickAction {
                if (particle.particleAlpha >= 0.9f) {
                    tick = false
                }
                if (particle.particleAlpha <= 0.2f) {
                    tick = true
                }
                particle.particleAlpha += if (tick) 0.05f else -0.05f
            }
        }
    }

    private fun withEffectEndRod(): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withSingle(
                ControlableEndRodEffect(it)
            )
        }.addParticleInstanceInit {
            colorOfRGB(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
            particleAlpha = 0.4f
            this.size = this@GoldenBallBarrageComposition.size
            currentAge = Random.nextInt(0, lifetime)
            textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
        }.addParticleControlerInstanceInit {
            var tick = true
            addPreTickAction {
                if (particle.particleAlpha >= 0.9f) {
                    tick = false
                }
                if (particle.particleAlpha <= 0.2f) {
                    tick = true
                }
                particle.particleAlpha += if (tick) 0.05f else -0.05f
            }
        }
    }

    override fun onDisplay() {
    }
}







