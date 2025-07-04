package cn.coostack.usefulmagic.gui

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.gui.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.recipe.RecipeEntry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class RecipeTutorialBookScreen(val recipe: RecipeEntry<AltarRecipeType>, parent: TutorialBookScreen) :
    RecipeTutorialBookMainScreen(parent) {

    companion object {
        val ALTAR_RECIPE_TABLE_TEXTURE = Identifier.of(UsefulMagic.MOD_ID, "textures/gui/altar_recipe_table.png")
        val slotPositions = ArrayList<IntArray>()
        var centerPosition = intArrayOf(65, 65)

        init {

            slotPositions.apply {
                // 下三行
                add(
                    intArrayOf(18, 112)
                )
                add(
                    intArrayOf(65, 130)
                )
                add(
                    intArrayOf(113, 112)
                )
                // 第二行的右边
                add(
                    intArrayOf(130, 65)
                )
                // 第一行的右边
                add(
                    intArrayOf(
                        113, 17
                    )
                )
                // 第一行的中间
                add(
                    intArrayOf(
                        65, 0
                    )
                )
                // 第一行的左边
                add(
                    intArrayOf(
                        18, 17
                    )
                )
                // 第二行的左边
                add(
                    intArrayOf(0, 65)
                )
            }


        }

    }

    override fun initContentIcons() {
        val value = recipe.value
        val center = value.center
        val round = value.round
        val x = (width / 2 + BACKGROUND_WIDTH / 4) - 166 / 2 - 5
        val y = getContentIconOriginY() + 20
        val player = client!!.player!!
        round.ingredients.forEachIndexed { index, it ->
            val pos = slotPositions[index]
            val item = it.matchingStacks[0]
            addDrawable(
                ItemTextureButton(x + pos[0], y + pos[1], 32, 32, item) {}
                    .apply {
                        val toolTips = item.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC)
                        var string = ""
                        toolTips.forEachIndexed { index, it ->
                            string += if (index == toolTips.size - 1) {
                                "${it?.string}"
                            } else {
                                "${it?.string}\n"
                            }
                        }
                        scale = 2f
                        tooltip = Tooltip.of(Text.literal(string))
                    }
            )
        }
        val item = center.matchingStacks[0]
        addDrawable(
            ItemTextureButton(x + centerPosition[0], y + centerPosition[1], 32, 32, item) {}
                .apply {
                    val toolTips = item.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC)
                    var string = ""
                    toolTips.forEachIndexed { index, it ->
                        string += if (index == toolTips.size - 1) {
                            "${it?.string}"
                        } else {
                            "${it?.string}\n"
                        }
                    }
                    tooltip = Tooltip.of(Text.literal(string))
                    scale = 2f
                }
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val x = (width / 2 + BACKGROUND_WIDTH / 4) - 166 / 2 - 5
        val y = getContentIconOriginY() + 20
        context.drawTexture(ALTAR_RECIPE_TABLE_TEXTURE, x, y, 0f, 0f, 165, 165, 165, 165)
        context.drawText(
            client!!.textRenderer, Text.literal(
                "§7§l合成所需魔力值§d${recipe.value.manaNeed}"
            ), x, y - 15, 0XFFFFFFFFU.toInt(), true
        )
    }

}