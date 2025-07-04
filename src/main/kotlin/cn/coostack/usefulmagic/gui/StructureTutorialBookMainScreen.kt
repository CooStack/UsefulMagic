package cn.coostack.usefulmagic.gui

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.widget.button.ItemTextureButton
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

/**
 * 子分类
 */
open class StructureTutorialBookMainScreen(val parent: TutorialBookScreen) : TutorialBookScreen() {
    override fun initTypeIcons() {
        val magicCore = ItemTextureButton(
            getTypedIconOriginX() + 20,
            getTypedIconOriginY() + 50,
            32,
            32,
            UsefulMagicBlocks.MAGIC_CORE.asItem().defaultStack
        ) {
            client!!.setScreen(AltarTutorialBookScreen(this))
        }.apply {
            tooltip = Tooltip.of(
                Text.literal(
                    "注魔祭坛"
                )
            )
            scale = 2f
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }
        addDrawableChild(magicCore)
    }

    override fun close() {
        client!!.setScreen(parent)
        client!!.soundManager.play(
            PositionedSoundInstance.master(
                SoundEvents.ITEM_BOOK_PAGE_TURN, 1f
            )
        )
    }
}