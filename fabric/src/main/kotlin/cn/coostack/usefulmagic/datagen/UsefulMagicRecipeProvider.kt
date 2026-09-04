package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancements.AdvancementRequirements
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapedRecipePattern
import net.minecraft.world.item.crafting.ShapelessRecipe
import net.minecraft.world.level.ItemLike
import java.util.concurrent.CompletableFuture

class UsefulMagicRecipeProvider(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) :
    FabricRecipeProvider(output, registriesFuture) {
    override fun buildRecipes(exporter: RecipeOutput) {

        saveShapeless(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.SPELL_BAG.getItem(),
            listOf(item(Items.LEATHER), item(Items.STRING), item(UsefulMagicItems.MANA_STAR.getItem())),
            conditionsFromItem(Items.LEATHER)
        )
        saveShaped(
            exporter, RecipeCategory.MISC, UsefulMagicItems.TUTORIAL_BOOK.getItem(),
            listOf(" T ", "YBR", " H "),
            mapOf(
                'T' to item(Items.SUGAR),
                'Y' to item(Items.GLOWSTONE_DUST),
                'R' to item(Items.REDSTONE),
                'H' to item(Items.GUNPOWDER),
                'B' to item(Items.BOOK)
            ),
            conditionsFromItem(Items.BOOK)
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.WOODEN_WAND.getItem(),
            listOf(" WW", " SW", "S W"),
            mapOf('W' to tag(ItemTags.PLANKS), 'S' to item(Items.STICK)),
            conditionsFromTag(ItemTags.PLANKS)
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.STONE_WAND.getItem(),
            listOf(" SS", " WS", "# S"),
            mapOf(
                'S' to item(Items.COBBLESTONE),
                '#' to item(Items.STICK),
                'W' to item(UsefulMagicItems.WOODEN_WAND.getItem())
            ),
            conditionsFromItem(UsefulMagicItems.WOODEN_WAND.getItem())
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.COPPER_WAND.getItem(),
            listOf(" CC", " SC", "E C"),
            mapOf(
                'C' to item(Items.COPPER_INGOT),
                'E' to item(Items.ENDER_PEARL),
                'S' to item(UsefulMagicItems.STONE_WAND.getItem())
            ),
            conditionsFromItem(UsefulMagicItems.STONE_WAND.getItem())
        )
        saveShaped(
            exporter, RecipeCategory.MISC, UsefulMagicItems.LARGE_MANA_BOTTLE.getItem(),
            listOf(" G ", "G G", "GGG"),
            mapOf('G' to item(Items.GLASS)),
            conditionsFromItem(Items.GLASS)
        )
        saveShaped(
            exporter, RecipeCategory.MISC, UsefulMagicItems.MANA_BOTTLE.getItem(), 4,
            listOf("G G", "GGG"),
            mapOf('G' to item(Items.GLASS)),
            conditionsFromItem(Items.GLASS)
        )
        saveShaped(
            exporter, RecipeCategory.MISC, UsefulMagicItems.SMALL_MANA_BOTTLE.getItem(), 8,
            listOf("G", "G"),
            mapOf('G' to item(Items.GLASS)),
            conditionsFromItem(Items.GLASS)
        )
        saveShaped(
            exporter, RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK_CORE.get(),
            listOf(" B ", "OEO", "OOO"),
            mapOf('O' to item(Items.OBSIDIAN), 'E' to item(Items.ENDER_EYE), 'B' to item(Items.BLAZE_POWDER)),
            conditionsFromItem(Items.OBSIDIAN)
        )
        saveShaped(
            exporter, RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.ALTAR_BLOCK.get(),
            listOf("OBO", "OOO"),
            mapOf('O' to item(Items.OBSIDIAN), 'B' to item(Items.BLAZE_POWDER)),
            conditionsFromItem(Items.OBSIDIAN)
        )
        saveShaped(
            exporter, RecipeCategory.BUILDING_BLOCKS, UsefulMagicBlocks.MAGIC_CORE.get(),
            listOf("OOO", "O O", "OOO"),
            mapOf('O' to item(Items.OBSIDIAN)),
            conditionsFromItem(Items.OBSIDIAN)
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.MANA_STAR.getItem(),
            listOf(" E ", "ERE", " E "),
            mapOf('E' to item(Items.ENDER_PEARL), 'R' to item(Items.REDSTONE)),
            conditionsFromItem(Items.ENDER_PEARL)
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.LASER_MAGIC.getItem(),
            listOf("BEB", "EME", "BEB"),
            mapOf(
                'E' to item(Items.ENDER_PEARL),
                'B' to item(Items.BLAZE_ROD),
                'M' to item(UsefulMagicItems.BARRAGE_MAGIC.getItem())
            ),
            conditionsFromItem(UsefulMagicItems.BARRAGE_MAGIC.getItem())
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.SWORD_QI_MAGIC.getItem(),
            listOf("AMA", "MSM", "AMA"),
            mapOf(
                'S' to item(Items.IRON_SWORD),
                'A' to item(Items.AMETHYST_SHARD),
                'M' to item(UsefulMagicItems.MANA_STAR.getItem())
            ),
            conditionsFromItem(Items.IRON_SWORD)
        )
        saveShaped(
            exporter, RecipeCategory.COMBAT, UsefulMagicItems.BARRAGE_MAGIC.getItem(),
            listOf(" I ", "IBI", " I "),
            mapOf('I' to item(Items.IRON_INGOT), 'B' to item(Items.BLAZE_ROD)),
            conditionsFromItem(UsefulMagicItems.BARRAGE_MAGIC.getItem())
        )
        saveShaped(
            exporter, RecipeCategory.FOOD, UsefulMagicItems.MANA_CRYSTAL.getItem(),
            listOf("CCC", "CMC", "CCC"),
            mapOf('C' to item(Items.AMETHYST_SHARD), 'M' to item(UsefulMagicItems.MANA_STAR.getItem())),
            conditionsFromItem(UsefulMagicItems.MANA_STAR.getItem())
        )

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.GOLDEN_MAGIC.getItem())
            .pattern("ECE")
            .pattern("CC")
            .pattern("ECE")
            .center(Items.GOLD_BLOCK)
            .input('E', Items.ENDER_EYE)
            .input('C', Items.AMETHYST_SHARD)
            .manaNeed(1000)
            .tick(600)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.SWORD_FORMATION_MAGIC.getItem())
            .pattern("CDC")
            .pattern("MM")
            .pattern("DMD")
            .center(UsefulMagicItems.SWORD_QI_MAGIC.getItem())
            .input('C', UsefulMagicItems.MANA_CRYSTAL.getItem())
            .input('D', Items.DIAMOND_SWORD)
            .input('M', UsefulMagicItems.MANA_STAR.getItem())
            .manaNeed(8000)
            .tick(800)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.HEALTH_MAGIC.getItem())
            .pattern("MCM")
            .pattern("CC")
            .pattern("MCM")
            .center(Items.ENCHANTED_GOLDEN_APPLE)
            .input('C', UsefulMagicItems.PURPLE_MANA_CRYSTAL.getItem())
            .input('M', UsefulMagicItems.PURPLE_MANA_STAR.getItem())
            .manaNeed(12000)
            .tick(1200)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.ANTI_ENTITY_DOMAIN_MAGIC.getItem())
            .pattern("CSC")
            .pattern("MM")
            .pattern("CMC")
            .center(UsefulMagicItems.BARRAGE_MAGIC.getItem())
            .input('C', UsefulMagicItems.RED_MANA_CRYSTAL.getItem())
            .input('M', UsefulMagicItems.RED_MANA_STAR.getItem())
            .input('S', Items.NETHER_STAR)
            .manaNeed(16000)
            .tick(1800)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.METEORITE_MAGIC.getItem())
            .pattern("OSO")
            .pattern("EE")
            .pattern("OSO")
            .center(Items.NETHERITE_BLOCK)
            .input('O', Items.OBSIDIAN)
            .input('E', Items.ENDER_PEARL)
            .input('S', Items.NETHER_STAR)
            .manaNeed(22000)
            .tick(2000)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.STARRY_MAGIC.getItem())
            .pattern("OOO")
            .pattern("SE")
            .pattern("OOO")
            .center(UsefulMagicItems.RED_MANA_STAR.getItem())
            .input('O', Items.OBSIDIAN)
            .input('E', UsefulMagicItems.ANTI_ENTITY_DOMAIN_MAGIC.getItem())
            .input('S', UsefulMagicItems.METEORITE_MAGIC.getItem())
            .manaNeed(30000)
            .tick(3000)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.LIGHTNING_MAGIC.getItem())
            .pattern("MIM")
            .pattern("AA")
            .pattern("MIM")
            .center(UsefulMagicItems.RED_MANA_STAR.getItem())
            .input('I', Items.IRON_BLOCK)
            .input('M', UsefulMagicItems.RED_MANA_STAR.getItem())
            .input('A', Items.AMETHYST_SHARD)
            .manaNeed(10000)
            .tick(3000)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.BLOOM_MAGIC.getItem())
            .pattern("MAM")
            .pattern("AA")
            .pattern("MAM")
            .center(Items.POPPY)
            .input('M', UsefulMagicItems.MANA_STAR.getItem())
            .input('A',Items.BONE_BLOCK)
            .manaNeed(2000)
            .tick(1000)
            .save(exporter)


        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.PUSH_MAGIC.getItem())
            .pattern("MAM")
            .pattern("AA")
            .pattern("MAM")
            .center(UsefulMagicItems.PURPLE_MANA_STAR.getItem())
            .input('M', Items.WIND_CHARGE)
            .input('A',Items.BREEZE_ROD)
            .manaNeed(2000)
            .tick(1000)
            .save(exporter)

        UsefulMagicAltarRecipeBuilder()
            .output(UsefulMagicItems.DIGGING_MAGIC.getItem())
            .pattern("MRM")
            .pattern("EE")
            .pattern("MRM")
            .center(Items.NETHERITE_PICKAXE)
            .input('M', UsefulMagicItems.PURPLE_MANA_STAR.getItem())
            .input('R', UsefulMagicItems.PURPLE_MANA_CRYSTAL.getItem())
            .input('E',Items.ENDER_EYE)
            .manaNeed(2000)
            .tick(1000)
            .save(exporter)
    }

    private fun conditionsFromTag(tags: TagKey<Item>): Criterion<*> {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tags).build())
    }

    private fun conditionsFromItem(obsidian: Item): Criterion<*> {
        return InventoryChangeTrigger.TriggerInstance.hasItems(obsidian)
    }

    private fun item(item: ItemLike): Ingredient {
        return Ingredient.of(item)
    }

    private fun tag(tag: TagKey<Item>): Ingredient {
        return Ingredient.of(tag)
    }

    private fun saveShapeless(
        exporter: RecipeOutput,
        category: RecipeCategory,
        result: ItemLike,
        ingredients: List<Ingredient>,
        criterion: Criterion<*>
    ) {
        val id = RecipeBuilder.getDefaultRecipeId(result)
        val advancement = exporter.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .addCriterion("has_item", criterion)
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR)
        val recipeIngredients = NonNullList.create<Ingredient>()
        recipeIngredients.addAll(ingredients)
        val recipe = ShapelessRecipe(
            "",
            RecipeBuilder.determineBookCategory(category),
            ItemStack(result.asItem()),
            recipeIngredients
        )

        exporter.accept(
            id,
            recipe,
            advancement.build(id.withPrefix("recipes/${category.folderName}/"))
        )
    }

    private fun saveShaped(
        exporter: RecipeOutput,
        category: RecipeCategory,
        result: ItemLike,
        rows: List<String>,
        definitions: Map<Char, Ingredient>,
        criterion: Criterion<*>
    ) {
        saveShaped(exporter, category, result, 1, rows, definitions, criterion)
    }

    private fun saveShaped(
        exporter: RecipeOutput,
        category: RecipeCategory,
        result: ItemLike,
        count: Int,
        rows: List<String>,
        definitions: Map<Char, Ingredient>,
        criterion: Criterion<*>
    ) {
        val id = RecipeBuilder.getDefaultRecipeId(result)
        val advancement = exporter.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .addCriterion("has_item", criterion)
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR)
        val recipe = ShapedRecipe(
            "",
            RecipeBuilder.determineBookCategory(category),
            ShapedRecipePattern.of(definitions, rows),
            ItemStack(result.asItem(), count),
            true
        )

        exporter.accept(
            id,
            recipe,
            advancement.build(id.withPrefix("recipes/${category.folderName}/"))
        )
    }

}
