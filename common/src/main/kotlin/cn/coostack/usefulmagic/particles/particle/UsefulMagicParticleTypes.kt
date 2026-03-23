package cn.coostack.usefulmagic.particles.particle

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredRegistry
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.serialization.MapCodec
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

object UsefulMagicParticleTypes {
    val particleTypes = mutableListOf<CommonDeferredRegistry<ParticleType<*>>>()

    @JvmField
    val WAVE_PARTICLE = register(
        "wave_particle",
        false,
        { WaveParticleEffect.codec },
        { WaveParticleEffect.packetCode }
    )

    private fun <T : ParticleOptions?> register(
        id: String,
        alwaysShow: Boolean,
        codecGetter: (type: ParticleType<T>) -> MapCodec<T>,
        packetCodec: (type: ParticleType<T>) -> StreamCodec<FriendlyByteBuf, T>
    ): CommonDeferredRegistry<ParticleType<T>> {
        val registry = CommonDeferredRegistry(
            BuiltInRegistries.PARTICLE_TYPE,
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        ) {
            object : ParticleType<T>(alwaysShow) {
                override fun codec(): MapCodec<T> {
                    return codecGetter(this)
                }

                override fun streamCodec(): StreamCodec<in RegistryFriendlyByteBuf, T> {
                    return packetCodec(this)
                }
            }
        }
        particleTypes.add(registry)
        @Suppress("UNCHECKED_CAST")
        return registry as CommonDeferredRegistry<ParticleType<T>>
    }

    fun init() {}
}
