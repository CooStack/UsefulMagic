package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockCoreEntity
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.item.Item
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult

class LargeManaBottle() : Item(Settings()) {

    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext?,
        tooltip: MutableList<Text?>,
        type: TooltipType?
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(
            Text.translatable(
                "item.large_mana_glass_bottle.usage"
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
        if (entity.currentMana < 1500) {
            return ActionResult.PASS
        }
        if (entity.crafting) {
            return ActionResult.PASS
        }
        // 粒子装填
        val hand = context.hand
        user.getStackInHand(hand).decrement(1)
        val stack = ItemStack(UsefulMagicItems.LARGE_MANA_REVIVE)
        stack.set(LargeManaRevive.LARGE_REVIVE_USE_COUNT, 1)
        if (!user.inventory.insertStack(stack)) {
            user.dropItem(stack, false)
        }
        world.playSound(
            null,
            pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1f, 1f
        )
        entity.currentMana -= 1500
        return super.useOnBlock(context)
    }

}