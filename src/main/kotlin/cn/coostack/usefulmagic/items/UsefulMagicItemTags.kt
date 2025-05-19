package cn.coostack.usefulmagic.items

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object UsefulMagicItemTags {

    val WAND = of("wand")

    val REVIVE = of("revive")

    val RELOADABLE_BOTTLE = of("reloadable_bottle")
    val STAR = of("star")
    val CRYSTAL = of("crystal")

    fun of(id: String): TagKey<Item> {
        return TagKey.of(
            RegistryKeys.ITEM, Identifier.of(UsefulMagic.MOD_ID, id)
        )
    }

}