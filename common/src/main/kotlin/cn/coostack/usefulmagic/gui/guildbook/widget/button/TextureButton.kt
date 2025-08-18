package cn.coostack.usefulmagic.gui.guildbook.widget.button

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component

class TextureButton(
    x: Int, y: Int, width: Int, height: Int, textures: WidgetSprites, pressAction: Button.OnPress
) : ImageButton(x, y, width, height, textures, pressAction) {
    var u = 0f
    var v = 0f
    var clickSound: SoundEvent = SoundEvents.UI_BUTTON_CLICK.value()
    var pitch = 1f
    var textureWidth = width
    var textureHeight = height
    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val texture = sprites.get(
            this.isActive, this.isHovered
        )
        context.blit(texture, this.x, this.y, u, v, width, height, textureWidth, textureHeight)
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(
            SimpleSoundInstance.forUI(clickSound, pitch)
        )
    }
}