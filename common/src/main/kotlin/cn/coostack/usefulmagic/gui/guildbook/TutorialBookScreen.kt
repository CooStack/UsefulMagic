package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.BetterTextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.TextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.gui.guildbook.widget.button.TextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW

open class TutorialBookScreen :
    Screen(Component.translatable("screen.usefulmagic.tutorial_book.screen_title")) {

    val client: Minecraft
        get() = Minecraft.getInstance()

    private lateinit var nextButton: TextureButton
    private lateinit var prevButton: TextureButton

    companion object {
        private const val KEY_PREFIX = "screen.usefulmagic.tutorial_book."

        val TUTORIAL_BOOK_MAIN_UI =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/tutorial_book_main_ui.png")
        val NEXT_PAGE = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/button/next_page.png")
        val NEXT_PAGE_HOVER =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/button/next_page_hover.png")
        val PREVIEW_PAGE =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/button/preview_page.png")
        val PREVIEW_PAGE_HOVER =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/button/preview_page_hover.png")
        val TUTORIAL_BOOK_TITLE =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/title/tutorial_book_title.png")

        const val BACKGROUND_WIDTH = 420
        const val BACKGROUND_HEIGHT = 256

        private const val TYPE_PANEL_WIDTH = 178
        private const val TYPE_PANEL_HEIGHT = 174
        private const val CONTENT_PANEL_WIDTH = 178
        private const val CONTENT_PANEL_HEIGHT = 216

        private const val PANEL_BODY_COLOR = 0x22201610
        private const val PANEL_HEADER_COLOR = 0x4D7A5A39
        private const val PANEL_BORDER_COLOR = 0x7AB69365
        private val PANEL_TEXT_COLOR = 0xFF6E5538u.toInt()
        private val PANEL_HINT_COLOR = 0xFF967859u.toInt()
        private val PAGE_TEXT_COLOR = 0xFFD9BC93u.toInt()
    }

    protected fun tb(key: String, vararg args: Any): Component = Component.translatable(KEY_PREFIX + key, *args)

    override fun init() {
        initTypeIcons()
        initContentIcons()
        initNavigationButtons()
        refreshPageButtons()
        super.init()
    }

    protected open fun showPageButtons(): Boolean = false

    protected open fun getCurrentPageIndex(): Int = 0

    protected open fun getPageCount(): Int = 1

    protected open fun getTypePanelTitle(): Component = tb("home.type_panel_title")

    protected open fun getContentPanelTitle(): Component = tb("home.content_panel_title")

    protected open fun getTypePanelHint(): Component = tb("home.type_panel_hint")

    protected open fun getContentPanelHint(): Component = tb("home.content_panel_hint")

    protected open fun getCloseTargetScreen(): Screen? = null

    open fun initContentIcons() {
        addRenderableOnly(
            BetterTextWidget(
                getContentIconOriginX(),
                getContentIconOriginY() + 30,
                165,
                120
            ).apply {
                shadow = false
                scaled = 1.1f
                heightPreLine = 12
                textColor = PANEL_TEXT_COLOR
                texts.add(tb("home.quick_start.title"))
                texts.add(tb("home.quick_start.line1"))
                texts.add(tb("home.quick_start.line2"))
                texts.add(tb("home.quick_start.line3"))
                texts.add(tb("home.quick_start.line4"))
            }
        )
    }

    open fun initTypeIcons() {
        val iconY = getTypedIconOriginY() + 66
        val structureX = getTypedIconOriginX() + 20
        val recipeX = getTypedIconOriginX() + 72
        val bestiaryX = getTypedIconOriginX() + 122

        val blockStructureType = ItemTextureButton(
            structureX,
            iconY,
            32,
            32,
            UsefulMagicBlocks.MAGIC_CORE.get().asItem().defaultInstance
        ) {
            client.setScreen(StructureTutorialBookMainScreen(this))
        }.apply {
            tooltip = Tooltip.create(tb("home.category.structure.tooltip"))
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        val recipe = ItemTextureButton(
            recipeX,
            iconY,
            32,
            32,
            UsefulMagicItems.IRON_WAND.getItem().defaultInstance
        ) {
            client.setScreen(RecipeTutorialBookMainScreen(this))
        }.apply {
            scale = 2f
            tooltip = Tooltip.create(tb("home.category.recipe.tooltip"))
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        val entity = ItemTextureButton(
            bestiaryX,
            iconY,
            32,
            32,
            Items.CAT_SPAWN_EGG.defaultInstance
        ) {}.apply {
            scale = 2f
            active = false
            tooltip = Tooltip.create(tb("home.category.bestiary.tooltip"))
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        addRenderableWidget(blockStructureType)
        addRenderableWidget(recipe)
        addRenderableWidget(entity)

        addRenderableOnly(
            TextWidget(
                tb("home.category.structure.label"),
                structureX + 16,
                iconY + 34,
                PANEL_TEXT_COLOR
            ).alignCenter()
        )
        addRenderableOnly(
            TextWidget(
                tb("home.category.recipe.label"),
                recipeX + 16,
                iconY + 34,
                PANEL_TEXT_COLOR
            ).alignCenter()
        )
        addRenderableOnly(
            TextWidget(
                tb("home.category.bestiary.label"),
                bestiaryX + 16,
                iconY + 34,
                PANEL_TEXT_COLOR
            ).alignCenter()
        )
    }

    open fun nextPage() {
    }

    open fun prevPage() {
    }

    protected fun refreshPageButtons() {
        if (!::nextButton.isInitialized || !::prevButton.isInitialized) return

        val pageCount = getPageCount().coerceAtLeast(1)
        val currentPage = getCurrentPageIndex().coerceIn(0, pageCount - 1)
        val canFlip = showPageButtons() && pageCount > 1

        nextButton.visible = canFlip
        prevButton.visible = canFlip
        nextButton.active = canFlip && currentPage < pageCount - 1
        prevButton.active = canFlip && currentPage > 0
    }

    private fun initNavigationButtons() {
        val rightX = getContentPanelX()
        val rightY = getContentPanelY()

        nextButton = TextureButton(
            rightX + CONTENT_PANEL_WIDTH - 34,
            rightY + CONTENT_PANEL_HEIGHT - 32,
            32,
            32,
            WidgetSprites(
                NEXT_PAGE,
                NEXT_PAGE_HOVER,
            )
        ) {
            nextPage()
            refreshPageButtons()
        }.apply {
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        prevButton = TextureButton(
            rightX + CONTENT_PANEL_WIDTH - 68,
            rightY + CONTENT_PANEL_HEIGHT - 32,
            32,
            32,
            WidgetSprites(
                PREVIEW_PAGE,
                PREVIEW_PAGE_HOVER,
            )
        ) {
            prevPage()
            refreshPageButtons()
        }.apply {
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        addRenderableWidget(nextButton)
        addRenderableWidget(prevButton)
    }

    override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderMenuBackground(context)
        val x = getBackgroundCenterX()
        val y = getBackgroundCenterY()
        context.blit(
            TUTORIAL_BOOK_MAIN_UI,
            x,
            y,
            0f,
            0f,
            BACKGROUND_WIDTH,
            BACKGROUND_HEIGHT,
            BACKGROUND_WIDTH,
            BACKGROUND_HEIGHT
        )

        drawPanel(context, getTypePanelX(), getTypePanelY(), TYPE_PANEL_WIDTH, TYPE_PANEL_HEIGHT)
        drawPanel(context, getContentPanelX(), getContentPanelY(), CONTENT_PANEL_WIDTH, CONTENT_PANEL_HEIGHT)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.blit(
            TUTORIAL_BOOK_TITLE,
            getTypedIconOriginX() + 10,
            getTypedIconOriginY() + 10,
            0f,
            0f,
            162,
            27,
            162,
            27
        )

        context.drawString(
            client.font,
            getTypePanelTitle(),
            getTypePanelX() + 6,
            getTypePanelY() + 5,
            PANEL_TEXT_COLOR,
            false
        )

        context.drawString(
            client.font,
            getContentPanelTitle(),
            getContentPanelX() + 6,
            getContentPanelY() + 5,
            PANEL_TEXT_COLOR,
            false
        )

        context.drawString(
            client.font,
            getTypePanelHint(),
            getTypePanelX() + 6,
            getTypePanelY() + TYPE_PANEL_HEIGHT - 12,
            PANEL_HINT_COLOR,
            false
        )

        val rightHintY = if (showPageButtons()) {
            getContentPanelY() + CONTENT_PANEL_HEIGHT - 41
        } else {
            getContentPanelY() + CONTENT_PANEL_HEIGHT - 12
        }

        context.drawString(
            client.font,
            getContentPanelHint(),
            getContentPanelX() + 6,
            rightHintY,
            PANEL_HINT_COLOR,
            false
        )

        if (showPageButtons() && getPageCount() > 1) {
            context.drawString(
                client.font,
                tb("page_info", getCurrentPageIndex() + 1, getPageCount()),
                getContentPanelX() + CONTENT_PANEL_WIDTH - 92,
                getContentPanelY() + CONTENT_PANEL_HEIGHT - 11,
                PAGE_TEXT_COLOR,
                false
            )
        }
    }

    protected fun getTypePanelX(): Int = getTypedIconOriginX() - 8

    protected fun getTypePanelY(): Int = getTypedIconOriginY() + 36

    protected fun getContentPanelX(): Int = getContentIconOriginX() - 8

    protected fun getContentPanelY(): Int = getContentIconOriginY() - 4

    fun getTypedIconOriginX(): Int = 22 + getBackgroundCenterX()

    fun getTypedIconOriginY(): Int = 20 + getBackgroundCenterY()

    fun getContentIconOriginX(): Int = 22 + width / 2

    fun getContentIconOriginY(): Int = getTypedIconOriginY()

    /**
     * 让给定宽度的元素水平居中（返回左上角 X）
     */
    fun getCenterX(itemWidth: Int): Int = (width - itemWidth) / 2

    /**
     * 让给定高度的元素垂直居中（返回左上角 Y）
     */
    fun getCenterY(itemHeight: Int): Int = (height - itemHeight) / 2

    private fun drawPanel(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int) {
        context.fill(x, y, x + width, y + height, PANEL_BODY_COLOR)
        context.fill(x + 1, y + 1, x + width - 1, y + 16, PANEL_HEADER_COLOR)
        context.fill(x + 1, y + 18, x + width - 1, y + 19, PANEL_HEADER_COLOR)

        context.fill(x, y, x + width, y + 1, PANEL_BORDER_COLOR)
        context.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER_COLOR)
        context.fill(x, y, x + 1, y + height, PANEL_BORDER_COLOR)
        context.fill(x + width - 1, y, x + width, y + height, PANEL_BORDER_COLOR)
    }

    private fun getBackgroundCenterX(): Int = (width - BACKGROUND_WIDTH) / 2

    private fun getBackgroundCenterY(): Int = (height - BACKGROUND_HEIGHT) / 2

    protected fun closeBookScreen() {
        val targetScreen = getCloseTargetScreen()
        if (targetScreen != null) {
            client.setScreen(targetScreen)
        } else {
            super.onClose()
        }
        client.soundManager.play(
            SimpleSoundInstance.forUI(
                SoundEvents.BOOK_PAGE_TURN,
                1f
            )
        )
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeBookScreen()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        closeBookScreen()
    }
}
