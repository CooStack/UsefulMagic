package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers as Buffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.util.math.Vec3d
import java.util.Random
import java.util.UUID

class EnchantLineStyle(
    var end: RelativeLocation,
    var count: Int,
    var maxAge: Int,
    uuid: UUID = UUID.randomUUID()
) :
    ParticleGroupStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val end = args["end"]!!.loadedValue as RelativeLocation
            val count = args["count"]!!.loadedValue as Int
            val maxAge = args["maxAge"]!!.loadedValue as Int
            return EnchantLineStyle(end, count, maxAge).also { it.readPacketArgs(args) }
        }

    }

    /**
     * 是否采用透明度淡入淡出
     */
    var fade: Boolean = false

    /**
     * 是否随机单个粒子的周期
     */
    var particleRandomAge: Boolean = true

    /**
     * 是否每tick随机一次粒子的周期
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
    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        return PointsBuilder()
            .addLine(RelativeLocation(), end, count)
            .createWithStyleData { withEffect() }
    }

    override fun onDisplay() {
        addPreTickAction {
            if (current++ >= maxAge) {
                remove()
                return@addPreTickAction
            }
            teleportTo(pos.add(speedDirection.toVector()))
        }
    }

    /**
     * 在beforeDisplay或者init执行才会生效
     */
    fun colorOf(vec: Vec3d) {
        this.r = vec.x.toInt().coerceIn(0, 255)
        this.g = vec.y.toInt().coerceIn(0, 255)
        this.b = vec.z.toInt().coerceIn(0, 255)
    }

    fun colorOf(r: Int, g: Int, b: Int) {
        this.r = r
        this.g = g
        this.b = b
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf(
            "end" to Buffers.relative(end),
            "count" to Buffers.int(count),
            "maxAge" to Buffers.int(maxAge),
            "fade" to Buffers.boolean(fade),
            "random_age" to Buffers.boolean(particleRandomAge),
            "random_age_tick" to Buffers.boolean(particleRandomAgePreTick),
            "current" to Buffers.int(current),
            "fade_in" to Buffers.int(fadeInTick),
            "fade_out" to Buffers.int(fadeOutTick),
            "particle_size" to Buffers.float(particleSize),
            "defaultAlpha" to Buffers.float(defaultAlpha),
            "speedDirection" to Buffers.relative(speedDirection),
            "r" to Buffers.int(r),
            "g" to Buffers.int(g),
            "b" to Buffers.int(b),
        )
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        args["end"]?.let { end = it.loadedValue as RelativeLocation }
        args["count"]?.let { count = it.loadedValue as Int }
        args["maxAge"]?.let { maxAge = it.loadedValue as Int }
        args["fade"]?.let { fade = it.loadedValue as Boolean }
        args["random_age"]?.let { particleRandomAge = it.loadedValue as Boolean }
        args["random_age_tick"]?.let { particleRandomAgePreTick = it.loadedValue as Boolean }
        args["current"]?.let { current = it.loadedValue as Int }
        args["fade_in"]?.let { fadeInTick = it.loadedValue as Int }
        args["fade_out"]?.let { fadeOutTick = it.loadedValue as Int }
        args["particle_size"]?.let { particleSize = it.loadedValue as Float }
        args["defaultAlpha"]?.let { defaultAlpha = it.loadedValue as Float }
        args["speedDirection"]?.let { speedDirection = it.loadedValue as RelativeLocation }
        args["r"]?.let { r = it.loadedValue as Int }
        args["g"]?.let { g = it.loadedValue as Int }
        args["b"]?.let { b = it.loadedValue as Int }
    }

    private fun withEffect(): StyleData = StyleData {
        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
    }.withParticleHandler {
        val random = Random(System.currentTimeMillis())
        this.colorOfRGB(r, g, b)
        this.size = particleSize
        this.textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
        if (particleRandomAge) {
            this.currentAge = random.nextInt(this.maxAge)
        }
    }.withParticleControlerHandler {
        val random = Random(System.currentTimeMillis())
        if (particleRandomAgePreTick) {
            addPreTickAction {
                this.currentAge = random.nextInt(this.maxAge)
            }
        }
        if (fade) {
            if (fadeInTick <= maxAge) {
                // 设置fadein
                val step = defaultAlpha / fadeInTick
                particle.particleAlpha = 0f
                addPreTickAction {
                    if (this@EnchantLineStyle.current > fadeInTick) {
                        return@addPreTickAction
                    }
                    particle.particleAlpha += step
                }
            }
            if (fadeOutTick <= maxAge) {
                val step = defaultAlpha / fadeOutTick
                particle.particleAlpha = defaultAlpha
                // 设置fadeout
                addPreTickAction {
                    if (this@EnchantLineStyle.current !in this@EnchantLineStyle.maxAge - fadeOutTick..this@EnchantLineStyle.maxAge) {
                        return@addPreTickAction
                    }
                    particle.particleAlpha -= step
                }
            }
        }
    }

}