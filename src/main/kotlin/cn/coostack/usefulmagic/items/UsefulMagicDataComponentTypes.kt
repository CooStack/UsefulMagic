package cn.coostack.usefulmagic.items

import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.serialization.Codec
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object UsefulMagicDataComponentTypes {
    @JvmStatic
    val LARGE_REVIVE_USE_COUNT = Registry.register(
        Registries.DATA_COMPONENT_TYPE, Identifier.of(UsefulMagic.MOD_ID, "large_revive_usage"),
        ComponentType.builder<Int>().codec(Codec.INT).build()
    )

    @JvmStatic
    val ENABLED = Registry.register(
        Registries.DATA_COMPONENT_TYPE, Identifier.of(UsefulMagic.MOD_ID, "enabled"),
        ComponentType.builder<Boolean>().codec(Codec.BOOL).build()
    )
    fun init() {

    }
}