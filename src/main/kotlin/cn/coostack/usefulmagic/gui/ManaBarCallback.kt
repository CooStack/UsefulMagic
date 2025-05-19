package cn.coostack.usefulmagic.gui

import cn.coostack.usefulmagic.managers.ClientManaManager
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.Text

class ManaBarCallback : HudRenderCallback {

    companion object {
        const val BAR_WIDTH = 81
        const val BAR_HEIGHT = 3
        const val BAR_Y_OFFSET = 39

        // rgb格式
        const val BACKGROUND_COLOR = 0XFF000000u
        const val FOREGROUND_COLOR = 0xFF00B7FFu
        const val BORDER_COLOR = 0xFF0044FFu
    }


    override fun onHudRender(
        context: DrawContext,
        tickCounter: RenderTickCounter
    ) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        if (player.isInCreativeMode) {
            return
        }
        // 消耗
        val window = client.window ?: return
        val x = 10
        val y = 20
        val mana = ClientManaManager.data.mana
        val max = ClientManaManager.data.maxMana.coerceAtLeast(1)
        val progress = mana.toDouble() / max

        context.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BACKGROUND_COLOR.toInt())
        val fillWidth = (BAR_WIDTH * progress).toInt()
        context.fill(x, y, x + fillWidth, y + BAR_HEIGHT, FOREGROUND_COLOR.toInt())
        context.drawBorder(x, y, BAR_WIDTH, BAR_HEIGHT, BORDER_COLOR.toInt());
        context.drawText(
            client.textRenderer, "魔力值: $mana/$max",
            x, y + BAR_HEIGHT, 0xFFFFFFFFu.toInt(), false
        )

    }
}