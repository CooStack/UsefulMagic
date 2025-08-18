package cn.coostack.usefulmagic.items

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

object UsefulMagicItemTags {

    val WAND = of("wand")

    val REVIVE = of("revive")

    val RELOADABLE_BOTTLE = of("reloadable_bottle")
    val STAR = of("star")
    val CRYSTAL = of("crystal")

    fun of(id: String): TagKey<Item> {
        return TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        )
    }

}