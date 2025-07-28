package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class SmallManaRevive(settings: Settings) : Item(settings) {
    val drinkTime = 20
    val manaRevive = 100

    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext?,
        tooltip: MutableList<Text?>,
        type: TooltipType?
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(
            Text.translatable(
                "item.small_mana_bottle.revive"
            )
        )
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.DRINK
    }

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        super.finishUsing(stack, world, user)
        if (user !is PlayerEntity) {
            return stack
        }


        if (!world.isClient) {
            // 增加魔力
            Criteria.CONSUME_ITEM.trigger(user as ServerPlayerEntity, stack)
            UsefulMagic.state.getDataFromServer(user.uuid).mana += manaRevive
        }

        if (stack.isEmpty) {
            return ItemStack(UsefulMagicItems.SMALL_MANA_BOTTLE)
        }
        if (!user.isInCreativeMode) {
            val stack = ItemStack(UsefulMagicItems.SMALL_MANA_BOTTLE)
            if (!user.inventory.insertStack(stack)) {
                user.dropItem(stack, false)
            }
        }
        return stack
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return drinkTime
    }

    override fun getDrinkSound(): SoundEvent? {
        return SoundEvents.ENTITY_GENERIC_DRINK
    }

    override fun getEatSound(): SoundEvent? {
        return SoundEvents.ENTITY_GENERIC_DRINK
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack?>? {
        val data = if (world.isClient) ClientManaManager.data else UsefulMagic.state.getDataFromServer(user.uuid)
        if (data.isFull() && !user.isInCreativeMode) {
            return TypedActionResult.fail(
                user.getStackInHand(hand)
            )
        }

        return ItemUsage.consumeHeldItem(world, user, hand)
    }
}