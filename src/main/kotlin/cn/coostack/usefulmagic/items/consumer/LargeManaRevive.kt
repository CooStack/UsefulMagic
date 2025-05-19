package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entitiy.AltarBlockCoreEntity
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntity
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.ClientManaManager
import com.mojang.serialization.Codec
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.component.ComponentType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class LargeManaRevive(settings: Settings) : Item(settings) {
    val drinkTime = 30
    val manaRevive = 1500

    companion object {
        const val MAX_USAGE = 3

        @JvmStatic
        val LARGE_REVIVE_USE_COUNT = Registry.register(
            Registries.DATA_COMPONENT_TYPE, Identifier.of(UsefulMagic.MOD_ID, "large_revive_usage"),
            ComponentType.builder<Int>().codec(Codec.INT).build()
        )
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext?,
        tooltip: MutableList<Text>,
        type: TooltipType?
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        val count = stack.get(LARGE_REVIVE_USE_COUNT) ?: let { stack.set(LARGE_REVIVE_USE_COUNT, MAX_USAGE) }
        tooltip.add(
            Text.translatable(
                "item.large_mana_bottle.revive"
            )
        )
        tooltip.add(
            Text.of(
                Text.translatable(
                    "item.large_mana_bottle.can_use_count"
                ).string.replace("%count%", "${stack.get(LARGE_REVIVE_USE_COUNT) ?: MAX_USAGE}")
            )
        )
        if (count != MAX_USAGE) {
            tooltip.add(
                Text.translatable(
                    "item.large_mana_glass_bottle.usage"
                )
            )
        }
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.DRINK
    }

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        val usage = stack.get(LARGE_REVIVE_USE_COUNT)
        super.finishUsing(stack, world, user)
        if (user !is PlayerEntity) {
            return stack
        }

        if (!world.isClient) {
            // 增加魔力
            Criteria.CONSUME_ITEM.trigger(user as ServerPlayerEntity, stack)
            UsefulMagic.state.getDataFromServer(user.uuid).mana += manaRevive
        }

        // NBT设置
        val newStack = ItemStack(UsefulMagicItems.LARGE_MANA_REVIVE)
        if (!user.isInCreativeMode) {
            if (usage == null) {
                newStack.set(LARGE_REVIVE_USE_COUNT, MAX_USAGE - 1)
            } else {
                newStack.set(LARGE_REVIVE_USE_COUNT, usage - 1)
            }
        } else if (usage == null) {
            newStack.set(LARGE_REVIVE_USE_COUNT, MAX_USAGE)
        }
        val drop = if (usage == 1) ItemStack(UsefulMagicItems.LARGE_MANA_BOTTLE) else newStack
        if (drop.isEmpty) {
            return drop
        }
        if (!user.isInCreativeMode) {
            if (!user.inventory.insertStack(drop)) {
                user.dropItem(drop, false)
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

    /**
     * 点击储魔方块进行装填操作
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
        val stack = user.getStackInHand(hand)
        val current = stack.get(LARGE_REVIVE_USE_COUNT) ?: MAX_USAGE
        if (current >= MAX_USAGE) return ActionResult.PASS
        val get = stack.copy().also {
            it.count = 1
            it.set(LARGE_REVIVE_USE_COUNT, current + 1)
        }
        if (!user.inventory.insertStack(get)) {
            user.dropItem(get, false)
        }
        stack.decrement(1)
        entity.currentMana -= 1500
        world.playSound(
            null,
            pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1f, 1f
        )
        return super.useOnBlock(context)
    }
}