package cn.coostack.usefulmagic.gui.guildbook.widget.button

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

class TextureButton(
    x: Int, y: Int, width: Int, height: Int, textures: ButtonTextures, pressAction: PressAction
) : TexturedButtonWidget(x, y, width, height, textures, pressAction) {
    var u = 0f
    var v = 0f
    var clickSound: SoundEvent = SoundEvents.UI_BUTTON_CLICK.value()
    var pitch = 1f
    var textureWidth = width
    var textureHeight = height
    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val texture = textures.get(
            this.isNarratable, this.isHovered
        )
        context.drawTexture(texture, this.x, this.y, u, v, width, height, textureWidth, textureHeight)
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(
            PositionedSoundInstance.master(clickSound, pitch)
        )
    }
}