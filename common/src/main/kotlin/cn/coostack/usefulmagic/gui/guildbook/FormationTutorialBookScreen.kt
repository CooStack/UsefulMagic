package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.BetterTextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class FormationTutorialBookScreen(parent: StructureTutorialBookMainScreen) : StructureTutorialBookMainScreen(parent) {

    private val formationPictureRenders = ArrayList<FormationTutorialBookScreen.(GuiGraphics) -> Unit>()
    private var current = -1

    override fun getTypePanelTitle(): Component = tb("formation.type_panel_title")

    override fun getContentPanelTitle(): Component = tb("formation.content_panel_title")

    override fun getTypePanelHint(): Component = tb("formation.type_panel_hint")

    override fun getContentPanelHint(): Component = tb("formation.content_panel_hint")

    override fun init() {
        initPage()
        super.init()
    }

    override fun initTypeIcons() {
        val iconY = getTypedIconOriginY() + 66
        val smallX = getTypedIconOriginX() + 18
        val midX = getTypedIconOriginX() + 72
        val largeX = getTypedIconOriginX() + 126

        addRenderableWidget(
            ItemTextureButton(
                smallX,
                iconY,
                32,
                32,
                UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.get().asItem().defaultInstance
            ) {
                current = 0
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.create(tb("formation.tooltip.small"))
        }

        addRenderableWidget(
            ItemTextureButton(
                midX,
                iconY,
                32,
                32,
                UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.get().asItem().defaultInstance
            ) {
                current = 1
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.create(tb("formation.tooltip.mid"))
        }

        addRenderableWidget(
            ItemTextureButton(
                largeX,
                iconY,
                32,
                32,
                UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.get().asItem().defaultInstance
            ) {
                current = 2
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.create(tb("formation.tooltip.large"))
        }
    }

    override fun initContentIcons() {
        if (current != -1) return

        addRenderableOnly(
            BetterTextWidget(
                getContentIconOriginX(),
                getContentIconOriginY() + 34,
                165,
                90
            ).apply {
                shadow = false
                scaled = 1.1f
                heightPreLine = 12
                textColor = 0xFF6E5538u.toInt()
                texts.add(tb("formation.guide.title"))
                texts.add(tb("formation.guide.line1"))
                texts.add(tb("formation.guide.line2"))
                texts.add(tb("formation.guide.line3"))
            }
        )
    }

    private fun initPage() {
        if (formationPictureRenders.isNotEmpty()) return

        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()

        fun texture(name: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/formation/$name")
        }

        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000u.toInt())
            it.blit(texture("small_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000u.toInt())
            it.blit(texture("mid_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000u.toInt())
            it.blit(texture("large_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (current in formationPictureRenders.indices) {
            formationPictureRenders[current](context)
        }
    }
}
