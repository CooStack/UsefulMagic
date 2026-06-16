package cn.coostack.usefulmagic.items.prop

import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack

data class SpellBagTooltip(
    val contents: List<ItemStack>,
    val capacity: Int,
) : TooltipComponent
