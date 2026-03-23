package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.TextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

/**
 * 结构教学的二级分类页面
 */
open class StructureTutorialBookMainScreen(val parent: TutorialBookScreen) : TutorialBookScreen() {

    override fun getTypePanelTitle(): Component = tb("structure.type_panel_title")

    override fun getContentPanelTitle(): Component = tb("structure.content_panel_title")

    override fun getTypePanelHint(): Component = tb("structure.type_panel_hint")

    override fun getContentPanelHint(): Component = tb("structure.content_panel_hint")

    override fun getCloseTargetScreen(): Screen = parent

    override fun initTypeIcons() {
        val iconY = getTypedIconOriginY() + 66
        val altarX = getTypedIconOriginX() + 32
        val formationX = getTypedIconOriginX() + 102

        val magicCore = ItemTextureButton(
            altarX,
            iconY,
            32,
            32,
            UsefulMagicBlocks.MAGIC_CORE.get().asItem().defaultInstance
        ) {
            client.setScreen(AltarTutorialBookScreen(this))
        }.apply {
            tooltip = Tooltip.create(tb("structure.category.altar.tooltip"))
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        val formation = ItemTextureButton(
            formationX,
            iconY,
            32,
            32,
            UsefulMagicBlocks.FORMATION_CORE_BLOCK.get().asItem().defaultInstance
        ) {
            client.setScreen(FormationTutorialBookScreen(this))
        }.apply {
            tooltip = Tooltip.create(tb("structure.category.formation.tooltip"))
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        addRenderableWidget(magicCore)
        addRenderableWidget(formation)

        addRenderableOnly(
            TextWidget(
                tb("structure.category.altar.label"),
                altarX + 16,
                iconY + 34,
                0xFF6E5538u.toInt()
            ).alignCenter()
        )

        addRenderableOnly(
            TextWidget(
                tb("structure.category.formation.label"),
                formationX + 16,
                iconY + 34,
                0xFF6E5538u.toInt()
            ).alignCenter()
        )
    }
}
