package cn.coostack.usefulmagic.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class BetterTextWidget(
    x: Int, y: Int, width: Int, height: Int
) : ClickableWidget(x, y, width, height, Text.empty()) {
    var shadow = false
    var scaled = 1f
    var textColor = 0xFFFFFFFFu.toInt()

    /**
     * 每一个元素代表一行
     */
    val texts = ArrayList<Text>()

    /**
     * 每一行的间距
     */
    var heightPreLine = 8
    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val client = MinecraftClient.getInstance()
        val textRenderer = client.textRenderer
        val matrices = context.matrices
        matrices.push()
        matrices.scale(scaled, scaled, 1f)
        matrices.translate(-x.toFloat(), -y.toFloat(), 0f)
        matrices.translate(x.toFloat() / scaled, y.toFloat() / scaled, 0f)
        var currentY = y
        texts.forEach {
            context.drawText(textRenderer, it, x, currentY, textColor, shadow)
            currentY += heightPreLine
        }

        matrices.pop()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
    }
}