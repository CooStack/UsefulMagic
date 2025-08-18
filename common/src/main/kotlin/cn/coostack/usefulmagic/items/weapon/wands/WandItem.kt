package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/**
 * @param cost 每次施法消耗魔力数量
 * @param damage 每个弹幕的伤害
 */
open class WandItem(
    settings: Properties, val cost: Int, val damage: Double
) : Item(settings) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.wand.damage"
                ).string.replace(
                    "%amount%", "$damage"
                )
            )
        )
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.wand.cost"
                ).string.replace("%cost%", "$cost")
            )
        )
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.wand.mana"
                ).string
                    .replace("%mana%", ClientManaManager.getSelfMana().mana.toString())
            )
        )
    }


    fun cost(player: ServerPlayer) {
        if (player.isCreative) {
            return
        }
        val data = UsefulMagic.state.getDataFromServer(player.uuid)
        data.mana -= cost
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = user.getItemInHand(hand)
        val canUse = if (!world.isClientSide) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            return InteractionResultHolder.fail(stack)
        }
        user.startUsingItem(hand)
        return InteractionResultHolder.consume(stack)
    }
}