package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

@CooAutoRegister
class EnchantBallBarrageComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var pColor: Vec3 = Vec3.ZERO

    @CodecField
    var size: Float = 1f

    @CodecField
    var r: Double = 2.0

    @CodecField
    var countPow: Int = 4
    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder().addBall(r, countPow * options / 2)
            .createWithCompositionData {
                CompositionData().setDisplayerSupplier {
                    ParticleDisplayer.withSingle(
                        ControlableEnchantmentEffect(it)
                    )
                }.addParticleInstanceInit {
                    colorOfRGB(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
                    this.size = this@EnchantBallBarrageComposition.size
                    particleAlpha = 0.4f
                    this.currentAge = Random.nextInt(0, lifetime)
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
    }

    override fun onDisplay() {
    }
}








