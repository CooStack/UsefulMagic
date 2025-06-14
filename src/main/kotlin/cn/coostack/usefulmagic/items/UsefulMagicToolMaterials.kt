package cn.coostack.usefulmagic.items

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey

enum class UsefulMagicToolMaterials(
    private val durability: Int,
    private val speedMultiplier: Float,
    private val attackDamage: Float,
    private val inverseTag: TagKey<Block?>,
    private val enchantability: Int,
    private val repairIngredient: Ingredient
) : ToolMaterial {
    MAGIC(
        2400, 9f, 5f, BlockTags.IRON_ORES, 15, Ingredient.ofItems(Items.IRON_INGOT)
    );

    override fun getDurability(): Int {
        return durability
    }

    override fun getMiningSpeedMultiplier(): Float {
        return speedMultiplier
    }

    override fun getAttackDamage(): Float {
        return attackDamage
    }

    override fun getInverseTag(): TagKey<Block?>? {
        return inverseTag
    }

    override fun getEnchantability(): Int {
        return enchantability
    }

    override fun getRepairIngredient(): Ingredient? {
        return repairIngredient
    }

}