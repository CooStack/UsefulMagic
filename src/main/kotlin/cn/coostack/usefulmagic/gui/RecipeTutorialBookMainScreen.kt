package cn.coostack.usefulmagic.gui

import cn.coostack.usefulmagic.gui.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

open class RecipeTutorialBookMainScreen(val parent: TutorialBookScreen) : TutorialBookScreen() {
    override fun initTypeIcons() {
        val startX = getTypedIconOriginX() + 10
        val startY = getTypedIconOriginY() + 50

        var currentX = startX
        var currentY = startY
        val stepX = 20
        val stepY = 20

        val maxX = width / 2 - 20
        val maxY = height - 20

        val player = client?.player ?: return
        val world = player.world ?: return
        val recipeManager = world.recipeManager
        val all = recipeManager.listAllOfType(AltarRecipeType.Type).associateBy { it.value.output.item }
        // 根据这个顺序来排列合成表
        UsefulMagicItems.items.forEach {
            if (it !in all) return@forEach
            val value = all[it]!!
            val output = value.value.output
            if (currentY > maxY) {
                return@forEach
            }
            addDrawableChild(
                ItemTextureButton(
                    currentX, currentY, 16, 16, output
                ) { btn ->
                    // 切换到对应的配方显示中
                    val screen = RecipeTutorialBookScreen(value, parent)
                    client!!.setScreen(screen)
                }
            ).apply {
                clickSound = SoundEvents.ITEM_BOOK_PAGE_TURN
                val toolTips = output.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC)
                var string = ""
                toolTips.forEachIndexed { index, it ->
                    string += if (index == toolTips.size - 1) {
                        "${it?.string}"
                    } else {
                        "${it?.string}\n"
                    }
                }
                tooltip = Tooltip.of(Text.literal(string))
            }
            currentX += stepX
            if (currentX > maxX) {
                currentX = startX
                currentY += stepY
            }
        }
    }

    override fun close() {
        client!!.setScreen(parent)
        client!!.soundManager.play(
            PositionedSoundInstance.master(
                SoundEvents.ITEM_BOOK_PAGE_TURN, 1f
            )
        )
    }
}