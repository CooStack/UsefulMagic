package cn.coostack.usefulmagic.gui.mana

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import kotlin.math.roundToInt

/**
 * neoforge 需要注册 RenderGameOverlayEvent.Post
 * fabric 需要注册 HudRenderCallback
 */
object ManaBarCallback {

    const val BAR_HEIGHT = 3

    @JvmStatic
    private val TEXTURE = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/mana_bar.png")


    fun onHudRender(
        context: GuiGraphics,
        tickCounter: Float
    ) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        if (player.isCreative || player.isSpectator) {
            return
        }
        // 消耗
        val window = client.window ?: return
        val matrices = context.pose()
        matrices.pushPose()
        RenderSystem.setShader(GameRenderer::getPositionShader)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)
        val x = 10
        val y = 20
        val mana = ClientManaManager.data.mana
        val max = ClientManaManager.data.maxMana.coerceAtLeast(1)
        val progress = mana.toDouble() / max
        val scale = (2 / window.guiScale).toFloat()
//        context.matrices.peek().normalMatrix.scale(scale)
        matrices.scale(scale, scale, 1f)
        context.blit(TEXTURE, x, y, 0, 0, 256, 40)
//      绘制魔力条
        val currentWidth = (213 * progress).roundToInt()
        context.blit(TEXTURE, x + 6, y + 23, 0, 44, currentWidth, 6)
        context.drawString(
            client.font, "魔力值: $mana/$max",
            x, y + BAR_HEIGHT, 0xFFFFFFFFu.toInt(), false
        )
//        context.matrices.scale(1f / scale, 1f / scale, 1f)
        matrices.popPose()
    }
}