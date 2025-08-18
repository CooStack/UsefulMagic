package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredRegistry
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab

object UsefulMagicItemGroups {
    @JvmField
    val usefulMagicMainGroup = CooParticlesServices.COO_REGISTRY
        .register(
            CommonDeferredRegistry(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "useful_magic_main")
            ) {
                CreativeModeTab.Builder(CreativeModeTab.Row.TOP, -1)
                    .title(Component.translatable("item.useful_magic_main"))
                    .icon { ItemStack(UsefulMagicItems.WOODEN_WAND.getItem()) }
                    .displayItems { _, entries ->
                        UsefulMagicItems.items.forEach {
                            entries.accept { it.getItem() }
                        }
                        UsefulMagicBlocks.blocks.forEach {
                            entries.accept { it.get().asItem() }
                        }
                    }
                    .build()
            }
        )


    fun init() {

    }

}