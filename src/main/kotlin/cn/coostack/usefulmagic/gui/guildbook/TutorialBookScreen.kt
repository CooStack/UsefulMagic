package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.gui.guildbook.widget.button.TextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

open class TutorialBookScreen() : Screen(Text.literal("bb")) {
    companion object {
        val TUTORIAL_BOOK_MAIN_UI = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/tutorial_book_main_ui.png")
        val NEXT_PAGE = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/button/next_page.png")
        val NEXT_PAGE_HOVER = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/button/next_page_hover.png")
        val PREVIEW_PAGE = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/button/preview_page.png")
        val PREVIEW_PAGE_HOVER = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/button/preview_page_hover.png")
        val TUTORIAL_BOOK_TITLE = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/title/tutorial_book_title.png")
        const val BACKGROUND_WIDTH = 420
        const val BACKGROUND_HEIGHT = 256
    }

    override fun init() {
        val originX = width / 2
        val originY = height / 2
        val next = TextureButton(
            originX + 150, originY + 80, 32, 32, ButtonTextures(
                NEXT_PAGE,
                NEXT_PAGE_HOVER,
            )
        ) {
            nextPage()
        }.apply {
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }
        val preview = TextureButton(
            originX + 108, originY + 81, 32, 32, ButtonTextures(
                PREVIEW_PAGE,
                PREVIEW_PAGE_HOVER,
            )
        ) {
            prevPage()
        }.apply {
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }
        initTypeIcons()
        initContentIcons()
        addDrawableChild(next)
        addDrawableChild(preview)
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
            UsefulMagicBlocks.MAGIC_CORE.asItem().defaultStack
        ) {
            val screen = StructureTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            tooltip = Tooltip.of(
                Text.literal(
                    "多方块结构"
                )
            )
            scale = 2f
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }

        val recipe = ItemTextureButton(
            getTypedIconOriginX() + 72,
            getTypedIconOriginY() + 50,
            32, 32, UsefulMagicItems.IRON_WAND.defaultStack
        ) {
            val screen = RecipeTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    "注魔祭坛合成配方"
                )
            )
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }
        val entity = ItemTextureButton(
            getTypedIconOriginX() + 122,
            getTypedIconOriginY() + 50,
            32, 32, Items.CAT_SPAWN_EGG.defaultStack
        ) {
            val screen = RecipeTutorialBookMainScreen(this)
            client!!.setScreen(screen)
        }.apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    "生物图鉴"
                )
            )
            clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
        }
        addDrawableChild(blockStructureType)
        addDrawableChild(recipe)
        addDrawableChild(entity)
    }

    open fun nextPage() {
    }

    open fun prevPage() {
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderDarkening(context)
        val x = getBackgroundCenterX()
        val y = getBackgroundCenterY()
        context.drawTexture(
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

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawTexture(
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
    override fun close() {
        super.close()
        client!!.soundManager.play(
            PositionedSoundInstance.master(
                SoundEvents.ITEM_BOOK_PAGE_TURN, 1f
            )
        )
    }
}
