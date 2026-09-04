package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.cparticle.CParticleUpdateMode
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*

@CooAutoRegister
class EnchantLineComposition(
    position: Vec3,
    world: Level?
) : AutoParticleComposition(position, world) {
    @CodecField
    var end: RelativeLocation = RelativeLocation.zero()

    @CodecField
    var count: Int = 0

    @CodecField
    var lifetime: Int = 0

    /**
     * 是否启用透明度淡入淡出。
     */
    var fade: Boolean = false

    /**
     * 是否随机设置单个粒子的生命周期。
     */
    var particleRandomAge: Boolean = true

    /**
     * 是否每 tick 重新随机粒子的生命周期。
     */
    var particleRandomAgePreTick: Boolean = true
    var fadeInTick = 10
    var fadeOutTick = 10
    var defaultAlpha = 0.8f

    @CodecField
    var color = Math3DUtil.colorOf(255, 255, 255)

    var current = 0
    var particleSize = 0.2f
    var speedDirection = RelativeLocation()
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addLine(RelativeLocation(), end, count)
            .createWithCompositionData {
                withEffect()
            }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (current++ >= lifetime) {
                remove()
                return@addPreTickAction
            }
            if (fade) {
                if (current == 1 && fadeInTick <= lifetime) {
                    playCParticleAlphaTransition(
                        durationTicks = fadeInTick.toFloat(),
                        alphaCurve = CParticleCurve.linear(0f, 1f),
                    )
                }
                if (current == lifetime - fadeOutTick && fadeOutTick <= lifetime) {
                    playCParticleAlphaTransition(
                        durationTicks = fadeOutTick.toFloat(),
                        alphaCurve = CParticleCurve.linear(1f, 0f),
                    )
                }
            }
            teleportTo(position.add(speedDirection.toVector()))
        }
    }

    /**
     * 仅在 beforeDisplay 或 init 阶段调用才会生效。
     */
    fun colorOf(vec: Vec3) {
        color = Math3DUtil.colorOf(
            vec.x.toInt().coerceIn(0, 255),
            vec.y.toInt().coerceIn(0, 255),
            vec.z.toInt().coerceIn(0, 255)
        )
    }

    fun colorOf(r: Int, g: Int, b: Int) {
        color = Math3DUtil.colorOf(r, g, b)
    }

    private fun withEffect(): CompositionData = CompositionData().setDisplayerSupplier {
        ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
    }.addCParticleInstanceInit {
        val random = Random(System.currentTimeMillis())
        effect = ControlableEnchantmentEffect(UUID.randomUUID())
        color = this@EnchantLineComposition.color
        this.size = particleSize
        alpha = if (fade) defaultAlpha else 1f
        if (particleRandomAgePreTick) {
            updateMode = CParticleUpdateMode.DYNAMIC
        }
        if (particleRandomAge) {
            age = random.nextInt(maxAge)
        }
    }.addCParticleControlerInstanceInit {
        val random = Random(System.currentTimeMillis())
        if (particleRandomAgePreTick) {
            addPreTickAction {
                age = random.nextInt(Int.MAX_VALUE)
            }
        }
    }

}

