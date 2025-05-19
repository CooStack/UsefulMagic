package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class UsefulMagicRecipeProvider(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) :
    FabricRecipeProvider(output, registriesFuture) {
    override fun generate(exporter: RecipeExporter) {
        ShapedRecipeJsonBuilder
            .create(RecipeCategory.COMBAT, UsefulMagicItems.WOODEN_WAND, 1)
            .pattern(" WW")
            .pattern(" SW")
            .pattern("S W")
            .criterion("has_item", conditionsFromTag(ItemTags.PLANKS))
            .input('W', ItemTags.PLANKS)
            .input('S', Items.STICK)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.COMBAT, UsefulMagicItems.STONE_WAND, 1)
            .pattern(" SS")
            .pattern(" WS")
            .pattern("# S")
            .criterion("has_item", conditionsFromItem(UsefulMagicItems.WOODEN_WAND))
            .input('S', Items.COBBLESTONE)
            .input('#', Items.STICK)
            .input('W', UsefulMagicItems.WOODEN_WAND)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.COMBAT, UsefulMagicItems.COPPER_WAND, 1)
            .criterion("has_item", conditionsFromItem(UsefulMagicItems.STONE_WAND))
            .pattern(" CC")
            .pattern(" SC")
            .pattern("E C")
            .input('C', Items.COPPER_INGOT)
            .input('E', Items.ENDER_PEARL)
            .input('S', UsefulMagicItems.STONE_WAND)
            .offerTo(exporter)


        ShapedRecipeJsonBuilder
            .create(RecipeCategory.MISC, UsefulMagicItems.LARGE_MANA_BOTTLE, 1)
            .pattern(" G ")
            .pattern("G G")
            .pattern("GGG")
            .criterion("has_item", conditionsFromItem(Items.GLASS))
            .input('G', Items.GLASS)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.MISC, UsefulMagicItems.MANA_BOTTLE, 4)
            .pattern("G G")
            .pattern("GGG")
            .criterion("has_item", conditionsFromItem(Items.GLASS))
            .input('G', Items.GLASS)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.MISC, UsefulMagicItems.SMALL_MANA_BOTTLE, 8)
            .pattern("G")
            .pattern("G")
            .criterion("has_item", conditionsFromItem(Items.GLASS))
            .input('G', Items.GLASS)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK_CORE, 1)
            .pattern(" B ")
            .pattern("OEO")
            .pattern("OOO")
            .criterion("has_item", conditionsFromItem(Items.OBSIDIAN))
            .input('O', Items.OBSIDIAN)
            .input('E', Items.ENDER_EYE)
            .input('B', Items.BLAZE_POWDER)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK, 1)
            .pattern("OBO")
            .pattern("OOO")
            .criterion("has_item", conditionsFromItem(Items.OBSIDIAN))
            .input('O', Items.OBSIDIAN)
            .input('B', Items.BLAZE_POWDER)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.MAGIC_CORE, 1)
            .pattern("OOO")
            .pattern("O O")
            .pattern("OOO")
            .criterion("has_item", conditionsFromItem(Items.OBSIDIAN))
            .input('O', Items.OBSIDIAN)
            .offerTo(exporter)

        ShapedRecipeJsonBuilder
            .create(RecipeCategory.COMBAT, UsefulMagicItems.MANA_STAR, 1)
            .criterion("has_item", conditionsFromItem(Items.ENDER_PEARL))
            .pattern(" E ")
            .pattern("ERE")
            .pattern(" E ")
            .input('E', Items.ENDER_PEARL)
            .input('R', Items.REDSTONE)
            .offerTo(exporter)


        ShapedRecipeJsonBuilder
            .create(RecipeCategory.FOOD, UsefulMagicItems.MANA_CRYSTAL, 1)
            .criterion("has_item", conditionsFromItem(UsefulMagicItems.MANA_STAR))
            .pattern("CCC")
            .pattern("CMC")
            .pattern("CCC")
            .input('C', Items.AMETHYST_SHARD)
            .input('M', UsefulMagicItems.MANA_STAR)
            .offerTo(exporter)
    }
}