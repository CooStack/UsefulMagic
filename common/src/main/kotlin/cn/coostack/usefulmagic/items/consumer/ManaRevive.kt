package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class ManaRevive(settings: Properties) : Item(settings) {
    val drinkTime = 30
    val manaRevive = 800

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip.add(
            Component.translatable(
                "item.mana_bottle.revive"
            )
        )
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.DRINK
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        super.finishUsingItem(stack, world, user)
        if (user !is Player) {
            return stack
        }

        if (!world.isClientSide) {
            // 增加魔力
            CriteriaTriggers.CONSUME_ITEM.trigger(user as ServerPlayer, stack)
            UsefulMagic.state.getDataFromServer(user.uuid).mana += manaRevive
        }

        if (stack.isEmpty) {
            return ItemStack(UsefulMagicItems.MANA_BOTTLE.getItem())
        }
        if (!user.isCreative) {
            val stack = ItemStack(UsefulMagicItems.MANA_BOTTLE.getItem())
            if (!user.inventory.add(stack)) {
                user.drop(stack, false)
            }
        }
        return stack
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return drinkTime
    }


    override fun getDrinkingSound(): SoundEvent {
        return SoundEvents.GENERIC_DRINK
    }

    override fun getEatingSound(): SoundEvent {
        return SoundEvents.GENERIC_DRINK
    }


    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val data = if (world.isClientSide) ClientManaManager.data else UsefulMagic.state.getDataFromServer(user.uuid)
        if (data.isFull() && !user.isCreative) {
            return InteractionResultHolder.fail(
                user.getItemInHand(hand)
            )
        }

        return ItemUtils.startUsingInstantly(world, user, hand)
    }
}