package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.world.item.Item
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag

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
        val world = player.level() ?: return
        val recipeManager = world.recipeManager
        val all = recipeManager
            .getAllRecipesFor(AltarRecipeType.Type)
            .associateBy { it.value.output.item }
        // 根据这个顺序来排列合成表
        val items = ArrayList<Item>()
        items.addAll(UsefulMagicItems.items.map { it.getItem() })
        items.addAll(
            UsefulMagicBlocks.blocks.map { it.get().asItem() }
        )
        items.forEach {
            if (it !in all) return@forEach
            val value = all[it]!!
            val output = value.value.output
            if (currentY > maxY) {
                return@forEach
            }
            addRenderableWidget(
                ItemTextureButton(
                    currentX, currentY, 16, 16, output
                ) { btn ->
                    // 切换到对应的配方显示中
                    val screen = RecipeTutorialBookScreen(value, parent)
                    client.setScreen(screen)
                }
            ).apply {
                clickSound = SoundEvents.BOOK_PAGE_TURN
                val toolTips = output.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                var string = ""
                toolTips.forEachIndexed { index, it ->
                    string += if (index == toolTips.size - 1) {
                        "${it?.string}"
                    } else {
                        "${it?.string}\n"
                    }
                }
                tooltip = Tooltip.create(Component.literal(string))
            }
            currentX += stepX
            if (currentX > maxX) {
                currentX = startX
                currentY += stepY
            }
        }
    }

    override fun onClose() {
        client.setScreen(parent)
        client.soundManager.play(
            SimpleSoundInstance.forUI(
                SoundEvents.BOOK_PAGE_TURN, 1f
            )
        )
    }
}