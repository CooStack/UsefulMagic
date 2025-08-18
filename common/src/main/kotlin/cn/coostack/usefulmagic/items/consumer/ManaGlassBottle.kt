package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext

class ManaGlassBottle : Item(Properties()) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.mana_glass_bottle.usage"
            )
        )
    }

    /**
     * 点击储魔方块可以变成其他物品
     */

    override fun useOn(context: UseOnContext): InteractionResult {
        val pos = context.clickedPos
        val world = context.level
        val user = context.player ?: return InteractionResult.PASS
        val entity = world.getBlockEntity(pos) ?: return InteractionResult.PASS
        if (entity !is MagicCoreBlockEntity) {
            return InteractionResult.PASS
        }
        if (entity.currentMana < 800) {
            return InteractionResult.PASS
        }
        if (entity.crafting) {
            return InteractionResult.PASS
        }
        // 粒子装填
        val hand = context.hand
        user.getItemInHand(hand).count -= 1
        val stack = ItemStack(UsefulMagicItems.MANA_REVIVE.getItem())
        if (!user.inventory.add(stack)) {
            user.drop(stack, false)
        }
        entity.currentMana -= 800
        world.playSound(
            null,
            pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1f, 1f
        )
        return super.useOn(context)
    }


}