package cn.coostack.usefulmagic.sounds

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredSoundEvent
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

object UsefulMagicSoundEvents {
    val soundEvents = mutableListOf<CommonDeferredSoundEvent>()

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
    fun register(name: String): CommonDeferredSoundEvent {
        val id = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, name)

        val common = CommonDeferredSoundEvent(id) {
            SoundEvent.createVariableRangeEvent(id)
        }
        soundEvents.add(common)
        return common
    }

    fun init() {}
}