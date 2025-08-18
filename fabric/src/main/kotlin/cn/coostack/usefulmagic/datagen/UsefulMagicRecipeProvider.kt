package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture
import kotlin.math.exp

class UsefulMagicRecipeProvider(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) :
    FabricRecipeProvider(output, registriesFuture) {
    override fun buildRecipes(exporter: RecipeOutput) {
        ShapedRecipeBuilder
            .shaped(RecipeCategory.MISC, UsefulMagicItems.TUTORIAL_BOOK.getItem(), 1)
            .pattern(" T ")
            .pattern("YBR")
            .pattern(" H ")
            .define('T', Items.SUGAR)
            .define('Y', Items.GLOWSTONE_DUST)
            .define('R', Items.REDSTONE)
            .define('H', Items.GUNPOWDER)
            .define('B', Items.BOOK)
            .unlockedBy("has_item", conditionsFromItem(Items.BOOK))
            .save(exporter)
        ShapedRecipeBuilder
            .shaped(RecipeCategory.COMBAT, UsefulMagicItems.WOODEN_WAND.getItem(), 1)
            .pattern(" WW")
            .pattern(" SW")
            .pattern("S W")
            .unlockedBy("has_item", conditionsFromTag(ItemTags.PLANKS))
            .define('W', ItemTags.PLANKS)
            .define('S', Items.STICK)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.COMBAT, UsefulMagicItems.STONE_WAND.getItem(), 1)
            .pattern(" SS")
            .pattern(" WS")
            .pattern("# S")
            .unlockedBy("has_item", conditionsFromItem(UsefulMagicItems.WOODEN_WAND.getItem()))
            .define('S', Items.COBBLESTONE)
            .define('#', Items.STICK)
            .define('W', UsefulMagicItems.WOODEN_WAND.getItem())
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.COMBAT, UsefulMagicItems.COPPER_WAND.getItem(), 1)
            .unlockedBy("has_item", conditionsFromItem(UsefulMagicItems.STONE_WAND.getItem()))
            .pattern(" CC")
            .pattern(" SC")
            .pattern("E C")
            .define('C', Items.COPPER_INGOT)
            .define('E', Items.ENDER_PEARL)
            .define('S', UsefulMagicItems.STONE_WAND.getItem())
            .save(exporter)


        ShapedRecipeBuilder
            .shaped(RecipeCategory.MISC, UsefulMagicItems.LARGE_MANA_BOTTLE.getItem(), 1)
            .pattern(" G ")
            .pattern("G G")
            .pattern("GGG")
            .unlockedBy("has_item", conditionsFromItem(Items.GLASS))
            .define('G', Items.GLASS)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.MISC, UsefulMagicItems.MANA_BOTTLE.getItem(), 4)
            .pattern("G G")
            .pattern("GGG")
            .unlockedBy("has_item", conditionsFromItem(Items.GLASS))
            .define('G', Items.GLASS)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.MISC, UsefulMagicItems.SMALL_MANA_BOTTLE.getItem(), 8)
            .pattern("G")
            .pattern("G")
            .unlockedBy("has_item", conditionsFromItem(Items.GLASS))
            .define('G', Items.GLASS)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK_CORE.get(), 1)
            .pattern(" B ")
            .pattern("OEO")
            .pattern("OOO")
            .unlockedBy("has_item", conditionsFromItem(Items.OBSIDIAN))
            .define('O', Items.OBSIDIAN)
            .define('E', Items.ENDER_EYE)
            .define('B', Items.BLAZE_POWDER)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK.get(), 1)
            .pattern("OBO")
            .pattern("OOO")
            .unlockedBy("has_item", conditionsFromItem(Items.OBSIDIAN))
            .define('O', Items.OBSIDIAN)
            .define('B', Items.BLAZE_POWDER)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.MAGIC_CORE.get(), 1)
            .pattern("OOO")
            .pattern("O O")
            .pattern("OOO")
            .unlockedBy("has_item", conditionsFromItem(Items.OBSIDIAN))
            .define('O', Items.OBSIDIAN)
            .save(exporter)

        ShapedRecipeBuilder
            .shaped(RecipeCategory.COMBAT, UsefulMagicItems.MANA_STAR.getItem(), 1)
            .unlockedBy("has_item", conditionsFromItem(Items.ENDER_PEARL))
            .pattern(" E ")
            .pattern("ERE")
            .pattern(" E ")
            .define('E', Items.ENDER_PEARL)
            .define('R', Items.REDSTONE)
            .save(exporter)
        ShapedRecipeBuilder
            .shaped(RecipeCategory.FOOD, UsefulMagicItems.MANA_CRYSTAL.getItem(), 1)
            .unlockedBy("has_item", conditionsFromItem(UsefulMagicItems.MANA_STAR.getItem()))
            .pattern("CCC")
            .pattern("CMC")
            .pattern("CCC")
            .define('C', Items.AMETHYST_SHARD)
            .define('M', UsefulMagicItems.MANA_STAR.getItem())
            .save(exporter)
    }

    private fun conditionsFromTag(tags: TagKey<Item>): Criterion<*> {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tags).build())
    }

    private fun conditionsFromItem(obsidian: Item): Criterion<*> {
        return InventoryChangeTrigger.TriggerInstance.hasItems(obsidian)
    }

}