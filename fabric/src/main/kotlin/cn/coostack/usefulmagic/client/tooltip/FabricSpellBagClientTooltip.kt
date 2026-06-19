package cn.coostack.usefulmagic.client.tooltip

import cn.coostack.usefulmagic.items.prop.SpellBagTooltip
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.resources.ResourceLocation

class FabricSpellBagClientTooltip(private val tooltip: SpellBagTooltip) : ClientTooltipComponent {
    override fun getHeight(): Int = backgroundHeight() + 4

    override fun getWidth(font: Font): Int = backgroundWidth()

    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        guiGraphics.blitSprite(BACKGROUND_SPRITE, x, y, backgroundWidth(), backgroundHeight())
        for (slotIndex in 0 until visibleSlotCount()) {
            val slotX = x + (slotIndex % GRID_WIDTH) * SLOT_SIZE_X + 1
            val slotY = y + (slotIndex / GRID_WIDTH) * SLOT_SIZE_Y + 1
            guiGraphics.blitSprite(SLOT_SPRITE, slotX, slotY, 0, SLOT_SIZE_X, SLOT_SIZE_Y)
            val stack = tooltip.contents.getOrNull(slotIndex) ?: continue
            guiGraphics.renderItem(stack, slotX + 1, slotY + 1, slotIndex)
            guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1)
            if (slotIndex == 0) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, slotX + 1, slotY + 1, 0)
            }
        }
    }

    private fun backgroundWidth(): Int = gridWidth() * SLOT_SIZE_X + 2

    private fun backgroundHeight(): Int = gridHeight() * SLOT_SIZE_Y + 2

    private fun gridWidth(): Int = visibleSlotCount().coerceAtMost(GRID_WIDTH).coerceAtLeast(1)

    private fun gridHeight(): Int = ((visibleSlotCount() + GRID_WIDTH - 1) / GRID_WIDTH).coerceAtLeast(1)

    private fun visibleSlotCount(): Int = (tooltip.contents.size + 1).coerceAtMost(tooltip.capacity)

    companion object {
        private val BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background")
        private val SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot")
        private const val GRID_WIDTH = 4
        private const val SLOT_SIZE_X = 18
        private const val SLOT_SIZE_Y = 20
    }
}
