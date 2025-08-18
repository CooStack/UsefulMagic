package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.gui.guildbook.widget.button.TextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items

open class TutorialBookScreen() : Screen(Component.literal("bb")) {

    val client: Minecraft
        get() = Minecraft.getInstance()

    companion object {
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
    }

    override fun init() {
        val originX = width / 2
        val originY = height / 2
        val next = TextureButton(
            originX + 150, originY + 80, 32, 32, WidgetSprites(
                NEXT_PAGE,
                NEXT_PAGE_HOVER,
            )
        ) {
            nextPage()
        }.apply {
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        val preview = TextureButton(
            originX + 108, originY + 81, 32, 32, WidgetSprites(
                PREVIEW_PAGE,
                PREVIEW_PAGE_HOVER,
            )
        ) {
            prevPage()
        }.apply {
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        initTypeIcons()
        initContentIcons()
//        addDrawableChild(ButtonWidget.builder(Component.literal("你好")) {}.bounds(0,0,32,32).build())
        addRenderableWidget(next)
        addRenderableWidget(preview)
        super.init()
    }

    open fun initContentIcons() {
    }

    /**
     * 处理左侧类别按钮
     */
    open fun initTypeIcons() {
        val centerX = width / 4
        val blockStructureType = ItemTextureButton(
            getTypedIconOriginX() + 20,
            getTypedIconOriginY() + 50,
            32,
            32,
            UsefulMagicBlocks.MAGIC_CORE.get().asItem().defaultInstance
        ) {
            val screen = StructureTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            tooltip = Tooltip.create(
                Component.literal(
                    "多方块结构"
                )
            )
            scale = 2f
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }

        val recipe = ItemTextureButton(
            getTypedIconOriginX() + 72,
            getTypedIconOriginY() + 50,
            32, 32, UsefulMagicItems.IRON_WAND.getItem().defaultInstance
        ) {
            val screen = RecipeTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            scale = 2f
            tooltip = Tooltip.create(
                Component.literal(
                    "注魔祭坛合成配方"
                )
            )
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        val entity = ItemTextureButton(
            getTypedIconOriginX() + 122,
            getTypedIconOriginY() + 50,
            32, 32, Items.CAT_SPAWN_EGG.defaultInstance
        ) {
            val screen = RecipeTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            scale = 2f
            tooltip = Tooltip.create(
                Component.literal(
                    "生物图鉴"
                )
            )
            clickSound = SoundEvents.BOOK_PAGE_TURN
        }
        addRenderableWidget(blockStructureType)
        addRenderableWidget(recipe)
        addRenderableWidget(entity)
    }

    open fun nextPage() {
    }

    open fun prevPage() {
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
    }

    fun getTypedIconOriginX(): Int = 22 + getBackgroundCenterX()
    fun getTypedIconOriginY(): Int = 20 + getBackgroundCenterY()

    fun getContentIconOriginX(): Int = 22 + width / 2
    fun getContentIconOriginY(): Int = getTypedIconOriginY()

    /**
     * 让输入的item居中 (居中的左上角坐标
     */
    fun getCenterX(itemWidth: Int): Int = (width - itemWidth) / 2

    /**
     * 让输入的item居中 (居中的左上角坐标
     */
    fun getCenterY(itemHeight: Int): Int = (height - itemHeight) / 2
    private fun getBackgroundCenterX(): Int = (width - BACKGROUND_WIDTH) / 2
    private fun getBackgroundCenterY(): Int = (height - BACKGROUND_HEIGHT) / 2
    override fun onClose() {
        super.onClose()
        client!!.soundManager.play(
            SimpleSoundInstance.forUI(
                SoundEvents.BOOK_PAGE_TURN, 1f
            )
        )
    }
}
