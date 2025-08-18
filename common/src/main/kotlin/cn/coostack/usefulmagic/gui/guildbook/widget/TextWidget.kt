package cn.coostack.usefulmagic.gui.guildbook.widget

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.realms.RealmsLabel

class TextWidget(text: Component, val x: Int, val y: Int, val color: Int) : RealmsLabel(text, x, y, color) {
    /**
     * 0 left
     * 1 center
     * 2 right
     */
    private var alignMethod = 1

    fun alignLeft(): TextWidget {
        alignMethod = 0
        return this
    }

    fun alignCenter(): TextWidget {
        alignMethod = 1
        return this
    }

    fun alignRight(): TextWidget {
        alignMethod = 2
        return this
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val visualOrderText = text.visualOrderText
        val font = Minecraft.getInstance().font
        val textWidth = font.width(visualOrderText)
        val drawX = when (alignMethod) {
            0 -> x
            1 -> x - textWidth / 2
            2 -> x - textWidth
            else -> 1
        }
        guiGraphics.drawString(font, visualOrderText, drawX, y, color)
    }
}