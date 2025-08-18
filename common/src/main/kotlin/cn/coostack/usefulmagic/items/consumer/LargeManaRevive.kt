package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

class LargeManaRevive(settings: Properties) : Item(settings) {
    val drinkTime = 30
    val manaRevive = 1500

    companion object {
        const val MAX_USAGE = 3
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {

        super.appendHoverText(stack, context, tooltip, tooltipFlag)

        val count =
            stack.get(LARGE_REVIVE_USE_COUNT.get()) ?: let { stack.set(LARGE_REVIVE_USE_COUNT.get(), MAX_USAGE) }
        tooltip.add(
            Component.translatable(
                "item.large_mana_bottle.revive"
            )
        )
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.large_mana_bottle.can_use_count"
                ).string.replace("%count%", "${stack.get(LARGE_REVIVE_USE_COUNT.get()) ?: MAX_USAGE}")
            )
        )
        if (count != MAX_USAGE) {
            tooltip.add(
                Component.translatable(
                    "item.large_mana_glass_bottle.usage"
                )
            )
        }
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.DRINK
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        val usage = stack.get(LARGE_REVIVE_USE_COUNT.get())
        super.finishUsingItem(stack, world, user)
        if (user !is Player) {
            return stack
        }

        if (!world.isClientSide) {
            // 增加魔力
            CriteriaTriggers.CONSUME_ITEM.trigger(user as ServerPlayer, stack)
            UsefulMagic.state.getDataFromServer(user.uuid).mana += manaRevive
        }

        // NBT设置
        val newStack = ItemStack(UsefulMagicItems.LARGE_MANA_REVIVE.getItem())
        if (!user.isCreative) {
            if (usage == null) {
                newStack.set(LARGE_REVIVE_USE_COUNT.get(), MAX_USAGE - 1)
            } else {
                newStack.set(LARGE_REVIVE_USE_COUNT.get(), usage - 1)
            }
        } else if (usage == null) {
            newStack.set(LARGE_REVIVE_USE_COUNT.get(), MAX_USAGE)
        }
        val drop = if (usage == 1) ItemStack(UsefulMagicItems.LARGE_MANA_BOTTLE.getItem()) else newStack
        if (drop.isEmpty) {
            return drop
        }
        if (!user.isCreative) {
            if (!user.inventory.add(drop)) {
                user.drop(drop, false)
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

    /**
     * 点击储魔方块进行装填操作
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
        val stack = user.getItemInHand(hand)
        val current = stack.get(LARGE_REVIVE_USE_COUNT.get()) ?: MAX_USAGE
        if (current >= MAX_USAGE) return InteractionResult.PASS
        val get = stack.copy().also {
            it.count = 1
            it.set(LARGE_REVIVE_USE_COUNT.get(), current + 1)
        }
        if (!user.inventory.add(get)) {
            user.drop(get, false)
        }
        stack.count -= 1
        entity.currentMana -= 1500
        world.playSound(
            null,
            pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1f, 1f
        )
        return super.useOn(context)
    }

}