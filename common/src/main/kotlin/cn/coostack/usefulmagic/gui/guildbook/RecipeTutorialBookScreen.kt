package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.world.item.Item
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.RecipeHolder

class RecipeTutorialBookScreen(val recipe: RecipeHolder<AltarRecipeType>, parent: TutorialBookScreen) :
    RecipeTutorialBookMainScreen(parent) {

    companion object {
        val ALTAR_RECIPE_TABLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/gui/altar_recipe_table.png")
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
            val item = it.items[0]
            addRenderableOnly(
                ItemTextureButton(
                    x + pos[0],
                    y + pos[1],
                    32,
                    32,
                    item
                ) {}
                    .apply {
                        val toolTips = item.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                        var string = ""
                        toolTips.forEachIndexed { index, it ->
                            string += if (index == toolTips.size - 1) {
                                "${it?.string}"
                            } else {
                                "${it?.string}\n"
                            }
                        }
                        scale = 2f
                        tooltip = Tooltip.create(Component.literal(string))
                    }
            )
        }
        val item = center.items[0]
        addRenderableOnly(
            ItemTextureButton(
                x + centerPosition[0],
                y + centerPosition[1],
                32,
                32,
                item
            ) {}
                .apply {
                    val toolTips = item.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                    var string = ""
                    toolTips.forEachIndexed { index, it ->
                        string += if (index == toolTips.size - 1) {
                            "${it?.string}"
                        } else {
                            "${it?.string}\n"
                        }
                    }
                    tooltip = Tooltip.create(Component.literal(string))
                    scale = 2f
                }
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val x = (width / 2 + BACKGROUND_WIDTH / 4) - 166 / 2 - 5
        val y = getContentIconOriginY() + 20
        context.blit(ALTAR_RECIPE_TABLE_TEXTURE, x, y, 0f, 0f, 165, 165, 165, 165)
        context.drawString(
            client.font, Component.literal(
                "§7§l合成所需魔力值§d${recipe.value.manaNeed}"
            ), x, y - 15, 0XFFFFFFFFU.toInt(), true
        )
    }

}