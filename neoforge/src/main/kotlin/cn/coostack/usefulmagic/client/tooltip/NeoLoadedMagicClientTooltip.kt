package cn.coostack.usefulmagic.client.tooltip

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.ItemStack

class NeoLoadedMagicClientTooltip(private val magicStack: ItemStack) : ClientTooltipComponent {
    override fun getHeight(): Int = 18

    override fun getWidth(font: Font): Int = 18

    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        guiGraphics.renderItem(magicStack, x + 1, y + 1, 0)
    }
}
