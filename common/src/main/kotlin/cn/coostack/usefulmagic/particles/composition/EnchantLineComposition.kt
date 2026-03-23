package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition

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
     * 鏄惁閲囩敤閫忔槑搴︽贰鍏ユ贰鍑?
     */
    var fade: Boolean = false

    /**
     * 鏄惁闅忔満鍗曚釜绮掑瓙鐨勫懆鏈?
     */
    var particleRandomAge: Boolean = true

    /**
     * 鏄惁姣弔ick闅忔満涓€娆＄矑瀛愮殑鍛ㄦ湡
     */
    var particleRandomAgePreTick: Boolean = false
    var fadeInTick = 10
    var fadeOutTick = 10
    var defaultAlpha = 0.8f
    var r = 255
    var g = 255
    var b = 255
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
            teleportTo(position.add(speedDirection.toVector()))
        }
    }

    /**
     * 鍦╞eforeDisplay鎴栬€卛nit鎵ц鎵嶄細鐢熸晥
     */
    fun colorOf(vec: Vec3) {
        this.r = vec.x.toInt().coerceIn(0, 255)
        this.g = vec.y.toInt().coerceIn(0, 255)
        this.b = vec.z.toInt().coerceIn(0, 255)
    }

    fun colorOf(r: Int, g: Int, b: Int) {
        this.r = r
        this.g = g
        this.b = b
    }

    private fun withEffect(): CompositionData = CompositionData().setDisplayerSupplier {
        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
    }.addParticleInstanceInit {
        val random = Random(System.currentTimeMillis())
        this.colorOfRGB(r, g, b)
        this.size = particleSize
        this.textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
        if (particleRandomAge) {
            this.currentAge = random.nextInt(this.lifetime)
        }
    }.addParticleControlerInstanceInit {
        val random = Random(System.currentTimeMillis())
        if (particleRandomAgePreTick) {
            addPreTickAction {
                this.currentAge = random.nextInt(this.lifetime)
            }
        }
        if (fade) {
            if (fadeInTick <= lifetime) {
                // 璁剧疆fadein
                val step = defaultAlpha / fadeInTick
                particle.particleAlpha = 0f
                addPreTickAction {
                    if (this@EnchantLineComposition.current > fadeInTick) {
                        return@addPreTickAction
                    }
                    particle.particleAlpha += step
                }
            }
            if (fadeOutTick <= lifetime) {
                val step = defaultAlpha / fadeOutTick
                particle.particleAlpha = defaultAlpha
                // 璁剧疆fadeout
                addPreTickAction {
                    if (this@EnchantLineComposition.current !in this@EnchantLineComposition.lifetime - fadeOutTick..this@EnchantLineComposition.lifetime) {
                        return@addPreTickAction
                    }
                    particle.particleAlpha -= step
                }
            }
        }
    }

}

