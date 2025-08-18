package cn.coostack.usefulmagic.items

import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.Item
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient

enum class UsefulMagicToolMaterials(
    private val durability: Int,
    private val speedMultiplier: Float,
    private val attackDamage: Float,
    private val inverseTag: TagKey<Block?>,
    private val enchantability: Int,
    private val repairIngredient: Ingredient
) : Tier {

    MAGIC(
        2400, 9f, 5f, BlockTags.IRON_ORES, 15, Ingredient.of(Items.IRON_INGOT)
    );


    override fun getUses(): Int = durability

    override fun getSpeed(): Float = speedMultiplier

    override fun getAttackDamageBonus(): Float = attackDamage

    override fun getIncorrectBlocksForDrops(): TagKey<Block?> {
        return inverseTag
    }

    override fun getEnchantmentValue(): Int = enchantability

    override fun getRepairIngredient(): Ingredient = repairIngredient
}