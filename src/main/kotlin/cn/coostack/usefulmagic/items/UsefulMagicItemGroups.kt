package cn.coostack.usefulmagic.items

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object UsefulMagicItemGroups {
    @JvmField
    val usefulMagicMainGroup = Registry.register(
        Registries.ITEM_GROUP, Identifier.of(UsefulMagic.MOD_ID, "useful_magic_main"),
        ItemGroup.Builder(null, -1)
            .displayName(Text.translatable("item.useful_magic_main"))
            .icon { ItemStack(UsefulMagicItems.WOODEN_WAND) }
            .entries { _, entries ->
                UsefulMagicItems.items.forEach(entries::add)
                UsefulMagicBlocks.blocks.forEach(entries::add)
            }
            .build()
    )

    fun init() {

    }

}