package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult

class SmallManaGlassBottle : Item(Settings()) {

    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext?,
        tooltip: MutableList<Text?>,
        type: TooltipType?
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(
            Text.translatable(
                "item.small_mana_glass_bottle.usage"
            )
        )
    }

    /**
     * 点击储魔方块可以变成其他物品
     */
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val pos = context.blockPos
        val world = context.world
        val user = context.player ?: return ActionResult.PASS
        val entity = world.getBlockEntity(pos) ?: return ActionResult.PASS
        if (entity !is MagicCoreBlockEntity) {
            return ActionResult.PASS
        }
        if (entity.currentMana < 100) {
            return ActionResult.PASS
        }
        if (entity.crafting) {
            return ActionResult.PASS
        }
        // 粒子装填
        val hand = context.hand
        user.getStackInHand(hand).decrement(1)
        val stack = ItemStack(UsefulMagicItems.SMALL_MANA_REVIVE)
        if (!user.inventory.insertStack(stack)) {
            user.dropItem(stack, false)
        }
        entity.currentMana -= 100
        world.playSound(null,
            pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS,1f,1f)
        return super.useOnBlock(context)
    }

}