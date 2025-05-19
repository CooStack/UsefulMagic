package cn.coostack.usefulmagic.recipe

import net.minecraft.item.ItemStack
import net.minecraft.recipe.input.RecipeInput

class AltarStackRecipeInput(
    val roundItems: List<ItemStack>,
    val centerItem: ItemStack,
    var mana: Int = 0
) : RecipeInput {
    override fun getStackInSlot(slot: Int): ItemStack {
        if (slot !in roundItems.indices) return ItemStack.EMPTY
        return roundItems[slot]
    }

    override fun getSize(): Int {
        return 8
    }
}