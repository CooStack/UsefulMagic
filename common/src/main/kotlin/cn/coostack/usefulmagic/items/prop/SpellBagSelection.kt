package cn.coostack.usefulmagic.items.prop

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

data class SpellBagSelection(
    val inventorySlot: Int,
    val stack: ItemStack,
    val contents: List<ItemStack>,
    val fallbackEmpty: Boolean
)

object SpellBagSelector {
    fun findFirstUsable(player: Player): SpellBagSelection? {
        var firstEmptyBag: SpellBagSelection? = null
        val inventory = player.inventory
        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            if (stack.item !is SpellBagItem) {
                continue
            }
            val contents = SpellBagItem.getMagicContentsForDisplay(stack)
            if (contents.isNotEmpty()) {
                return SpellBagSelection(slot, stack, contents, false)
            }
            if (firstEmptyBag == null) {
                firstEmptyBag = SpellBagSelection(slot, stack, contents, true)
            }
        }
        return firstEmptyBag
    }
}
