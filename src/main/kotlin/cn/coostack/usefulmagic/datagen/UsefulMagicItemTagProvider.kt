package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.items.UsefulMagicItemTags
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.UsefulMagicItems.MANA_STAR
import cn.coostack.usefulmagic.items.UsefulMagicItems.PURPLE_MANA_CRYSTAL
import cn.coostack.usefulmagic.items.UsefulMagicItems.PURPLE_MANA_STAR
import cn.coostack.usefulmagic.items.UsefulMagicItems.RED_MANA_CRYSTAL
import cn.coostack.usefulmagic.items.UsefulMagicItems.RED_MANA_STAR
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class UsefulMagicItemTagProvider(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider.ItemTagProvider(output, completableFuture) {
    override fun configure(lookup: RegistryWrapper.WrapperLookup) {

        getOrCreateTagBuilder(ItemTags.AXES)
            .add(UsefulMagicItems.MAGIC_AXE)

        getOrCreateTagBuilder(UsefulMagicItemTags.WAND)
            .add(
                UsefulMagicItems.WOODEN_WAND,
                UsefulMagicItems.STONE_WAND,
                UsefulMagicItems.COPPER_WAND,
                UsefulMagicItems.IRON_WAND,
                UsefulMagicItems.GOLDEN_WAND,
                UsefulMagicItems.DIAMOND_WAND,
                UsefulMagicItems.NETHERITE_WAND,
                UsefulMagicItems.WAND_OF_METEORITE,
                UsefulMagicItems.ANTI_ENTITY_WAND,
                UsefulMagicItems.HEALTH_REVIVE_WAND,
                UsefulMagicItems.LIGHTNING_WAND,
                UsefulMagicItems.EXPLOSION_WAND,
            )
        getOrCreateTagBuilder(UsefulMagicItemTags.REVIVE)
            .add(
                UsefulMagicItems.MANA_REVIVE,
                UsefulMagicItems.SMALL_MANA_REVIVE,
                UsefulMagicItems.LARGE_MANA_REVIVE,
            )
        getOrCreateTagBuilder(UsefulMagicItemTags.RELOADABLE_BOTTLE)
            .add(
                UsefulMagicItems.MANA_BOTTLE,
                UsefulMagicItems.LARGE_MANA_BOTTLE,
                UsefulMagicItems.SMALL_MANA_BOTTLE,
                UsefulMagicItems.LARGE_MANA_REVIVE,
            )

        getOrCreateTagBuilder(UsefulMagicItemTags.STAR)
            .add(MANA_STAR)
            .add(PURPLE_MANA_STAR)
            .add(RED_MANA_STAR)

        getOrCreateTagBuilder(UsefulMagicItemTags.CRYSTAL)
            .add(UsefulMagicItems.MANA_CRYSTAL, PURPLE_MANA_CRYSTAL, RED_MANA_CRYSTAL)
    }
}