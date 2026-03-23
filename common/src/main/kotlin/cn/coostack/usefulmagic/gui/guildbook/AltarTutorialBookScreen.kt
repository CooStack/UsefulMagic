package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.gui.guildbook.widget.BetterTextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.TextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks

class AltarTutorialBookScreen(parent: StructureTutorialBookMainScreen) : StructureTutorialBookMainScreen(parent) {
    val pageRender = ArrayList<AltarTutorialBookScreen.(context: GuiGraphics) -> Unit>()
    val pageWidgets = ArrayList<AltarTutorialBookScreen.() -> Unit>()

    override fun showPageButtons(): Boolean = pageRender.size > 1

    override fun getCurrentPageIndex(): Int = page

    override fun getPageCount(): Int = pageRender.size.coerceAtLeast(1)

    override fun getTypePanelTitle(): Component = tb("altar.type_panel_title")

    override fun getContentPanelTitle(): Component = tb("altar.content_panel_title")

    override fun getTypePanelHint(): Component = tb("altar.type_panel_hint")

    override fun getContentPanelHint(): Component = tb("altar.content_panel_hint")

    private fun getStepTexture(step: Int): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID, "textures/gui/steps/step${step}.png"
        )
    }

    override fun init() {
        initPages()
        initPageWidgets()
        super.init()
    }

    override fun initTypeIcons() {
        val startX = getTypedIconOriginX() + 5
        val startY = getTypedIconOriginY() + 60
        addRenderableOnly(
            TextWidget(
                tb("altar.support.table_title"),
                startX + 20,
                getTypedIconOriginY() + 40,
                0xFFc5b091U.toInt()
            ).alignLeft()
        )

        var currentX = startX
        var currentY = startY
        val stepX = 35
        val stepY = 35

        val maxX = width / 2 - 20
        client.player ?: return

        fun nextCurrentX(): Int {
            val old = currentX
            currentX += stepX
            return old
        }

        fun nextCurrentY(): Int {
            val old = currentY
            if (currentX > maxX) {
                currentX = startX
                currentY += stepY
            }
            return old
        }

        fun addSupportBlock(item: ItemStack, tooltipKey: String) {
            addRenderableWidget(
                ItemTextureButton(
                    nextCurrentX(),
                    nextCurrentY(),
                    32,
                    32,
                    item
                ) {}
            ).apply {
                scale = 2f
                tooltip = Tooltip.create(tb(tooltipKey))
            }
        }

        addSupportBlock(Blocks.COAL_BLOCK.asItem().defaultInstance, "altar.support.tooltip.coal")
        addSupportBlock(Blocks.IRON_BLOCK.asItem().defaultInstance, "altar.support.tooltip.iron")
        addSupportBlock(Blocks.GOLD_BLOCK.asItem().defaultInstance, "altar.support.tooltip.gold")
        addSupportBlock(Blocks.REDSTONE_BLOCK.asItem().defaultInstance, "altar.support.tooltip.redstone")
        addSupportBlock(Blocks.DIAMOND_BLOCK.asItem().defaultInstance, "altar.support.tooltip.diamond")
        addSupportBlock(Blocks.EMERALD_BLOCK.asItem().defaultInstance, "altar.support.tooltip.emerald")
        addSupportBlock(Blocks.NETHERITE_BLOCK.asItem().defaultInstance, "altar.support.tooltip.netherite")
    }

    private fun initPages() {
        if (pageRender.isNotEmpty()) return
        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.blit(getStepTexture(1), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.blit(getStepTexture(2), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.blit(getStepTexture(3), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.blit(getStepTexture(4), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.blit(getStepTexture(5), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 77, 0x8F000000U.toInt())
            it.blit(getStepTexture(6), contentX, contentY, 0F, 0F, 150, 75, 150, 75)
        }
    }

    private fun initPageWidgets() {
        if (pageWidgets.isNotEmpty()) return
        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()

        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 120,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.1.line1"))
                }
            )
        }
        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 120,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.2.line1"))
                }
            )
        }
        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 120,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.3.line1"))
                    texts.add(tb("altar.step.3.line2"))
                }
            )
        }
        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 120,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.4.line1"))
                    texts.add(tb("altar.step.4.line2"))
                    texts.add(tb("altar.step.4.line3"))
                    texts.add(tb("altar.step.4.line4"))
                }
            )
        }
        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 120,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.5.line1"))
                }
            )
        }
        pageWidgets.add {
            addRenderableOnly(
                BetterTextWidget(
                    contentX,
                    contentY + 80,
                    140,
                    16 * 6
                ).apply {
                    shadow = false
                    scaled = 1.2f
                    heightPreLine = 12
                    textColor = 0xFFc5b091u.toInt()
                    texts.add(tb("altar.step.6.line1"))
                }
            )
        }
    }

    var page = 0
    override fun initContentIcons() {
        pageWidgets.getOrNull(page)?.invoke(this)
    }

    override fun nextPage() {
        if (page == pageRender.size - 1) return
        page++
        rebuildWidgets()
    }

    override fun prevPage() {
        if (page == 0) return
        page--
        rebuildWidgets()
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (pageRender.isNotEmpty()) {
            pageRender[page](context)
        }
    }
}
