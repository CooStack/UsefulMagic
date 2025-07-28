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
    val MAGIC_ACTIVATE = register("magic_activate")

    @JvmStatic
    val MAGIC_SWORD = register("magic_sword")

    @JvmStatic
    val SKY_FALLING_MAGIC_IDLE = register("sky_falling_magic_idle")

    @JvmStatic
    val SKY_FALLING_MAGIC_START = register("sky_falling_magic_start")
    @JvmStatic
    val DEFEND_SHIELD_HIT = register("defend_shield_hit")

    @JvmStatic
    fun register(name: String): SoundEvent {
        val id = Identifier.of(UsefulMagic.MOD_ID, name)
        return Registry.register(
            Registries.SOUND_EVENT, id, SoundEvent.of(id)
        )
    }

    fun init() {}
}