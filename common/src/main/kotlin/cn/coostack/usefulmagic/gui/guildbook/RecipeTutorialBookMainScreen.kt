package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.BetterTextWidget
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.screens.Screen
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.RecipeHolder
import kotlin.math.min

open class RecipeTutorialBookMainScreen(val parent: TutorialBookScreen) : TutorialBookScreen() {

    private val recipes = ArrayList<RecipeHolder<AltarRecipeType>>()
    private var recipesLoaded = false
    private var page = 0

    private val columns = 8
    private val rows = 6
    private val itemStepX = 20
    private val itemStepY = 20

    private val itemsPerPage: Int
        get() = columns * rows

    override fun init() {
        loadRecipesIfNeeded()
        page = page.coerceIn(0, getPageCount() - 1)
        super.init()
    }

    override fun showPageButtons(): Boolean = getPageCount() > 1

    override fun getCurrentPageIndex(): Int = page

    override fun getPageCount(): Int {
        if (recipes.isEmpty()) return 1
        return (recipes.size + itemsPerPage - 1) / itemsPerPage
    }

    override fun getTypePanelTitle(): Component = tb("recipe_main.type_panel_title")

    override fun getContentPanelTitle(): Component = tb("recipe_main.content_panel_title")

    override fun getTypePanelHint(): Component = tb("recipe_main.type_panel_hint")

    override fun getContentPanelHint(): Component = tb("recipe_main.content_panel_hint")

    override fun getCloseTargetScreen(): Screen = parent

    override fun initTypeIcons() {
        val player = client.player ?: return

        if (recipes.isEmpty()) {
            addRenderableOnly(
                BetterTextWidget(
                    getTypedIconOriginX() + 8,
                    getTypedIconOriginY() + 74,
                    165,
                    40
                ).apply {
                    shadow = false
                    scaled = 1.1f
                    heightPreLine = 12
                    textColor = 0xFF6E5538u.toInt()
                    texts.add(tb("recipe_main.empty.line1"))
                    texts.add(tb("recipe_main.empty.line2"))
                }
            )
            return
        }

        val startX = getTypedIconOriginX() + 8
        val startY = getTypedIconOriginY() + 58

        val startIndex = page * itemsPerPage
        val endIndex = min(startIndex + itemsPerPage, recipes.size)

        for (index in startIndex until endIndex) {
            val recipeHolder = recipes[index]
            val local = index - startIndex
            val row = local / columns
            val col = local % columns
            val x = startX + col * itemStepX
            val y = startY + row * itemStepY

            val output = recipeHolder.value.output
            addRenderableWidget(
                ItemTextureButton(
                    x,
                    y,
                    16,
                    16,
                    output
                ) {
                    client.setScreen(RecipeTutorialBookScreen(recipeHolder, this))
                }
            ).apply {
                clickSound = SoundEvents.BOOK_PAGE_TURN
                val tooltipText = output
                    .getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                    .joinToString("\n") { it.string }
                tooltip = Tooltip.create(tb("raw", tooltipText))
            }
        }
    }

    override fun initContentIcons() {
        addRenderableOnly(
            BetterTextWidget(
                getContentIconOriginX(),
                getContentIconOriginY() + 34,
                165,
                95
            ).apply {
                shadow = false
                scaled = 1.1f
                heightPreLine = 12
                textColor = 0xFF6E5538u.toInt()
                texts.add(tb("recipe_main.guide.title"))
                texts.add(tb("recipe_main.guide.line1"))
                texts.add(tb("recipe_main.guide.line2"))
                texts.add(tb("recipe_main.guide.line3"))
                texts.add(tb("recipe_main.guide.count", recipes.size))
            }
        )
    }

    override fun nextPage() {
        if (page >= getPageCount() - 1) return
        page++
        rebuildWidgets()
    }

    override fun prevPage() {
        if (page <= 0) return
        page--
        rebuildWidgets()
    }

    private fun loadRecipesIfNeeded() {
        if (recipesLoaded) return

        val player = client.player ?: return
        recipesLoaded = true
        val world = player.level()
        val recipeManager = world.recipeManager

        val allByOutput = recipeManager
            .getAllRecipesFor(AltarRecipeType.Type)
            .associateBy { it.value.output.item }

        val orderedItems = ArrayList<Item>()
        orderedItems.addAll(UsefulMagicItems.items.map { it.getItem() })
        orderedItems.addAll(UsefulMagicBlocks.blocks.map { it.get().asItem() })

        val ordered = LinkedHashSet<RecipeHolder<AltarRecipeType>>()
        orderedItems.forEach { item ->
            allByOutput[item]?.let { ordered.add(it) }
        }
        allByOutput.values.forEach { ordered.add(it) }

        recipes.clear()
        recipes.addAll(ordered)
    }
}
