package cn.coostack.usefulmagic.gui.guildbook.widget

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class BetterTextWidget(
    x: Int, y: Int, width: Int, height: Int
) : AbstractWidget(x, y, width, height, Component.empty()) {
    var shadow = false
    var scaled = 1f
    var textColor = 0xFFFFFFFFu.toInt()

    /**
     * 每一个元素代表一行
     */
    val texts = ArrayList<Component>()

    /**
     * 每一行的间距
     */
    var heightPreLine = 8
    override fun renderWidget(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val client = Minecraft.getInstance()
        val textRenderer = client.font
        val matrices = context.pose()
        matrices.pushPose()
        matrices.scale(scaled, scaled, 1f)
        matrices.translate(-x.toFloat(), -y.toFloat(), 0f)
        matrices.translate(x.toFloat() / scaled, y.toFloat() / scaled, 0f)
        var currentY = y
        texts.forEach {
            context.drawString(textRenderer, it, x, currentY, textColor, shadow)
            currentY += heightPreLine
        }

        matrices.popPose()
    }

    override fun updateWidgetNarration(p0: NarrationElementOutput) {

    }


}