package cn.coostack.usefulmagic.sounds

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object UsefulMagicSoundEvents {
    @JvmStatic
    val ELECTRIC_EFFECT = register("electric_effect")

    @JvmStatic
    fun register(name: String): SoundEvent {
        val id = Identifier.of(UsefulMagic.MOD_ID, name)
        return Registry.register(
            Registries.SOUND_EVENT, id, SoundEvent.of(id)
        )
    }

    fun init() {}
}