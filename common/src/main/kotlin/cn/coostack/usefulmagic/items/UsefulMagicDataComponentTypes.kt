package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredComponentType
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier

object UsefulMagicDataComponentTypes {
    val types = mutableListOf<CommonDeferredComponentType<*>>()

    @JvmStatic
    val LARGE_REVIVE_USE_COUNT = register("large_revive_usage"){
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    @JvmStatic
    val ENABLED = register(
        "enabled"
    ) {
        DataComponentType.builder<Boolean>().persistent(Codec.BOOL).build()
    }

    fun <T> register(id: String, type: Supplier<DataComponentType<T>>): CommonDeferredComponentType<T> {
        val location = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        val common = CommonDeferredComponentType(location, type)
        types.add(common)
        return common
    }

    fun init() {

    }
}