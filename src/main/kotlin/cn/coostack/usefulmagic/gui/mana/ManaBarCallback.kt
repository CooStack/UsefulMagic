package cn.coostack.usefulmagic.gui.mana

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import kotlin.math.roundToInt

class ManaBarCallback : HudRenderCallback {

    companion object {
        const val BAR_HEIGHT = 3

        @JvmStatic
        private val TEXTURE = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/mana_bar.png")
    }


    override fun onHudRender(
        context: DrawContext,
        tickCounter: RenderTickCounter
    ) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        if (player.isInCreativeMode || player.isSpectator) {
            return
        }
        // 消耗
        val window = client.window ?: return
        val matrices = context.matrices
        matrices.push()
        RenderSystem.setShader(GameRenderer::getPositionProgram)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)
        val x = 10
        val y = 20
        val mana = ClientManaManager.data.mana
        val max = ClientManaManager.data.maxMana.coerceAtLeast(1)
        val progress = mana.toDouble() / max
        val scale = (2 / window.scaleFactor).toFloat()
//        context.matrices.peek().normalMatrix.scale(scale)
        context.matrices.scale(scale, scale, 1f)
        context.drawTexture(TEXTURE, x, y, 0, 0, 256, 40)
//      绘制魔力条
        val currentWidth = (213 * progress).roundToInt()
        context.drawTexture(TEXTURE, x + 6, y + 23, 0, 44, currentWidth, 6)
        context.drawText(
            client.textRenderer, "魔力值: $mana/$max",
            x, y + BAR_HEIGHT, 0xFFFFFFFFu.toInt(), false
        )
//        context.matrices.scale(1f / scale, 1f / scale, 1f)
        matrices.pop()
    }
}