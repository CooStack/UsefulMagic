package cn.coostack.usefulmagic.gui.widget.button

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import kotlin.math.roundToInt

class ItemTextureButton(
    x: Int, y: Int, width: Int, height: Int, val item: ItemStack, pressAction: PressAction,
) : ButtonWidget(x, y, width, height, Text.empty(), pressAction, DEFAULT_NARRATION_SUPPLIER) {
    var scale = 1f
    var clickSound: SoundEvent = SoundEvents.UI_BUTTON_CLICK.value()
    var pitch = 1f
    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.matrices
        matrices.push()
        matrices.scale(scale, scale, 1f)
        matrices.translate(-x.toFloat(), -y.toFloat(), 0f)
        matrices.translate(x.toFloat() / scale, y.toFloat() / scale, 0f)
        context.drawItem(item, x, y)
        if (this.isHovered) {
            context.fillGradient(
                RenderLayer.getGuiOverlay(),
                x,
                y,
                x + 16,
                y + 16,
                0x7FFFFFFFU.toInt(),
                0x7FFFFFFFU.toInt(),
                0
            )
        }
        matrices.pop()
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(
            PositionedSoundInstance.master(clickSound, pitch)
        )
    }
}