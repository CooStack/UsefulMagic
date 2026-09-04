package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.RecipeHolder

class RecipeTutorialBookScreen(
    val recipe: RecipeHolder<AltarRecipeType>,
    private val recipeListScreen: RecipeTutorialBookMainScreen
) : RecipeTutorialBookMainScreen(recipeListScreen.parent) {

    companion object {
        val ALTAR_RECIPE_TABLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/altar_recipe_table.png")

        val slotPositions = listOf(
            intArrayOf(18, 112),
            intArrayOf(65, 130),
            intArrayOf(113, 112),
            intArrayOf(130, 65),
            intArrayOf(113, 17),
            intArrayOf(65, 0),
            intArrayOf(18, 17),
            intArrayOf(0, 65)
        )

        val centerPosition = intArrayOf(65, 65)
    }

    override fun getContentPanelTitle(): Component = tb("recipe_detail.content_panel_title")

    override fun getTypePanelHint(): Component = tb("recipe_detail.type_panel_hint")

    override fun getContentPanelHint(): Component = tb("recipe_detail.content_panel_hint")

    override fun getCloseTargetScreen() = recipeListScreen

    override fun getRecipeListScreen(): RecipeTutorialBookMainScreen = recipeListScreen

    override fun initContentIcons() {
        val value = recipe.value
        val center = value.center
        val round = value.round
        val x = (width / 2 + BACKGROUND_WIDTH / 4) - 166 / 2 - 5
        val y = getContentIconOriginY() + 20
        val player = client.player ?: return

        round.ingredients.forEachIndexed { index, ingredient ->
            val pos = slotPositions[index]
            val item = ingredient.items[0]
            addRenderableOnly(
                ItemTextureButton(
                    x + pos[0],
                    y + pos[1],
                    32,
                    32,
                    item
                ) {}
                    .apply {
                        val tooltipText = item
                            .getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                            .joinToString("\n") { it.string }
                        scale = 2f
                        tooltip = Tooltip.create(tb("raw", tooltipText))
                    }
            )
        }

        val centerItem = center.items[0]
        addRenderableOnly(
            ItemTextureButton(
                x + centerPosition[0],
                y + centerPosition[1],
                32,
                32,
                centerItem
            ) {}
                .apply {
                    val tooltipText = centerItem
                        .getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                        .joinToString("\n") { it.string }
                    tooltip = Tooltip.create(tb("raw", tooltipText))
                    scale = 2f
                }
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val x = (width / 2 + BACKGROUND_WIDTH / 4) - 166 / 2 - 5
        val y = getContentIconOriginY() + 20
        val manaText = tb("recipe_detail.mana_need", recipe.value.manaNeed)
        val manaTextX = getContentPanelX() + 178 - 6 - client.font.width(manaText)
        val manaTextY = getContentPanelY() + 5

        context.blit(ALTAR_RECIPE_TABLE_TEXTURE, x, y, 0f, 0f, 165, 165, 165, 165)
        context.drawString(
            client.font,
            manaText,
            manaTextX,
            manaTextY,
            0xFFDFC79Eu.toInt(),
            false
        )
    }
}
