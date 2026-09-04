package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random
import java.util.UUID

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
                    ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
                }.addCParticleInstanceInit {
                    effect = ControlableEnchantmentEffect(UUID.randomUUID())
                    color = Math3DUtil.colorOf(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
                    this.size = this@EnchantBallBarrageComposition.size
                    alpha = 0.4f
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
    }

    override fun onDisplay() {
    }
}








