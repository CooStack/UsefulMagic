package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.extend.isFullMana
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.extend.maxMana
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level

class SmallManaRevive(settings: Properties) : Item(settings) {
    val drinkTime = 20
    val manaRevive = 100

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip.add(
            Component.translatable(
                "item.small_mana_bottle.revive"
            )
        )
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.DRINK
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, user: LivingEntity): ItemStack {
        super.finishUsingItem(stack, level, user)
        if (user !is Player) {
            return stack
        }

        if (!level.isClientSide) {
            // 增加魔力
            CriteriaTriggers.CONSUME_ITEM.trigger(user as ServerPlayer, stack)
            user.mana += manaRevive
            user.mana = user.mana.coerceAtMost(user.maxMana)
        }

        if (stack.isEmpty) {
            return ItemStack(UsefulMagicItems.SMALL_MANA_BOTTLE.getItem())
        }
        if (!user.isCreative) {
            val stack = ItemStack(UsefulMagicItems.SMALL_MANA_BOTTLE.getItem())
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
        if (user.isFullMana() && !user.isCreative) {
            return InteractionResultHolder.fail(
                user.getItemInHand(hand)
            )
        }

        return ItemUtils.startUsingInstantly(world, user, hand)
    }
}
