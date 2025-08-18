package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component

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
            UsefulMagicBlocks.MAGIC_CORE.get().asItem().defaultInstance
        ) {
            client.setScreen(AltarTutorialBookScreen(this))
        }.apply {
            tooltip = Tooltip.create(
                Component.literal(
                    "注魔祭坛"
                )
            )
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        val formation = ItemTextureButton(
            getTypedIconOriginX() + 62,
            getTypedIconOriginY() + 50,
            32, 32,
            UsefulMagicBlocks.FORMATION_CORE_BLOCK.get().asItem().defaultInstance
        ) {
            client.setScreen(FormationTutorialBookScreen(this))
        }.apply {
            tooltip = Tooltip.create(
                Component.literal(
                    "阵法"
                )
            )
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        addRenderableWidget(magicCore)
        addRenderableWidget(formation)
    }

    override fun onClose() {
        client.setScreen(parent)
        client.soundManager.play(
            SimpleSoundInstance.forUI(
                SoundEvents.BOOK_PAGE_TURN, 1f
            )
        )
    }
}