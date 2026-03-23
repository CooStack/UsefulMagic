package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import cn.coostack.usefulmagic.recipe.RoundShapeRecipe
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

/**
 * Pattern rows map to:
 * row 1 -> 654
 * row 2 -> 7 3
 * row 3 -> 012
 */
class UsefulMagicAltarRecipeBuilder {
    private val definitions = LinkedHashMap<Char, Ingredient>()
    private val patterns = mutableListOf<String>()
    private var output = ItemStack.EMPTY
    private var center: Ingredient? = null
    private var manaNeed = 0
    private var tick = 0

    fun output(item: ItemLike, count: Int = 1): UsefulMagicAltarRecipeBuilder = apply {
        require(count > 0) { "Result count must be greater than 0" }
        output = ItemStack(item.asItem(), count)
    }

    fun output(stack: ItemStack): UsefulMagicAltarRecipeBuilder = apply {
        require(!stack.isEmpty) { "Output stack cannot be empty" }
        output = stack.copy()
    }

    fun manaNeed(manaNeed: Int): UsefulMagicAltarRecipeBuilder = apply {
        require(manaNeed >= 0) { "Mana cost cannot be negative" }
        this.manaNeed = manaNeed
    }

    fun tick(tick: Int): UsefulMagicAltarRecipeBuilder = apply {
        require(tick >= 0) { "Tick cost cannot be negative" }
        this.tick = tick
    }

    fun input(symbol: Char, item: ItemLike): UsefulMagicAltarRecipeBuilder {
        return input(symbol, Ingredient.of(item))
    }

    fun input(symbol: Char, ingredient: Ingredient): UsefulMagicAltarRecipeBuilder = apply {
        require(symbol != ' ') { "Space is reserved for empty slots" }
        require(!ingredient.isEmpty) { "Ingredient for '$symbol' cannot be empty" }
        definitions[symbol] = ingredient
    }

    fun center(item: ItemLike): UsefulMagicAltarRecipeBuilder {
        return center(Ingredient.of(item))
    }

    fun center(ingredient: Ingredient): UsefulMagicAltarRecipeBuilder = apply {
        require(!ingredient.isEmpty) { "Center ingredient cannot be empty" }
        center = ingredient
    }

    fun pattern(pattern: String): UsefulMagicAltarRecipeBuilder = apply {
        val rowIndex = patterns.size
        require(rowIndex < ROW_TO_SLOTS.size) { "Altar recipe only supports 3 rows" }
        require(pattern.length == ROW_TO_SLOTS[rowIndex].size) {
            "Pattern row ${rowIndex + 1} must be exactly ${ROW_TO_SLOTS[rowIndex].size} characters"
        }
        require(pattern.none { it == ' ' }) { "Use omitted rows instead of spaces" }
        patterns += pattern
    }

    fun save(exporter: RecipeOutput) {
        save(exporter, defaultRecipeId())
    }

    fun save(exporter: RecipeOutput, recipePath: String) {
        save(exporter, ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, recipePath))
    }

    fun save(exporter: RecipeOutput, recipeId: ResourceLocation) {
        exporter.accept(recipeId, build(), null)
    }

    fun build(): AltarRecipeType {
        require(!output.isEmpty) { "Output is not defined" }
        require(patterns.isNotEmpty()) { "Altar recipe must define at least one pattern row" }

        val ingredients = NonNullList.withSize(8, Ingredient.EMPTY)
        patterns.forEachIndexed { rowIndex, pattern ->
            val slots = ROW_TO_SLOTS[rowIndex]
            pattern.forEachIndexed { columnIndex, symbol ->
                ingredients[slots[columnIndex]] = ingredientAt(symbol)
            }
        }

        return AltarRecipeType(
            output = output.copy(),
            round = RoundShapeRecipe(ingredients),
            center = center ?: error("Center ingredient is not defined"),
            manaNeed = manaNeed,
            tick = tick
        )
    }

    private fun defaultRecipeId(): ResourceLocation {
        val itemId = BuiltInRegistries.ITEM.getKey(output.item)
        return ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, itemId.path)
    }

    private fun ingredientAt(symbol: Char): Ingredient {
        return definitions[symbol] ?: error("Pattern symbol '$symbol' is not defined")
    }

    companion object {
        private val ROW_TO_SLOTS = listOf(
            intArrayOf(6, 5, 4),
            intArrayOf(7, 3),
            intArrayOf(0, 1, 2)
        )
    }
}
