package cn.coostack.usefulmagic.gui.guildbook.widget.button

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.world.item.ItemStack
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

class ItemTextureButton(
    x: Int, y: Int, width: Int, height: Int, val item: ItemStack, pressAction: OnPress,
) : Button(x, y, width, height, Component.empty(), pressAction, DEFAULT_NARRATION) {
    var scale = 1f
    var clickSound: SoundEvent = SoundEvents.UI_BUTTON_CLICK.value()
    var pitch = 1f
    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        matrices.pushPose()
        matrices.scale(scale, scale, 1f)
        matrices.translate(-x.toFloat(), -y.toFloat(), 0f)
        matrices.translate(x.toFloat() / scale, y.toFloat() / scale, 0f)
        context.renderItem(item, x, y)
        if (this.isHovered) {
            context.fillGradient(
                RenderType.guiOverlay(),
                x,
                y,
                x + 16,
                y + 16,
                0x7FFFFFFFU.toInt(),
                0x7FFFFFFFU.toInt(),
                0
            )
        }
        matrices.popPose()
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(
            SimpleSoundInstance.forUI(clickSound, pitch)
        )
    }
}