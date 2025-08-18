package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext

class LargeManaBottle() : Item(Properties()) {


    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable(
                "item.large_mana_glass_bottle.usage"
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
        if (entity.currentMana < 1500) {
            return InteractionResult.PASS
        }
        if (entity.crafting) {
            return InteractionResult.PASS
        }
        // 粒子装填
        val hand = context.hand
        user.getItemInHand(hand).count -= 1
        val stack = ItemStack(UsefulMagicItems.LARGE_MANA_REVIVE.getItem())
        stack.set(LARGE_REVIVE_USE_COUNT.get(), 1)
        if (!user.inventory.add(stack)) {
            user.drop(stack, false)
        }
        world.playSound(
            null,
            pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1f, 1f
        )
        entity.currentMana -= 1500
        return super.useOn(context)
    }

}