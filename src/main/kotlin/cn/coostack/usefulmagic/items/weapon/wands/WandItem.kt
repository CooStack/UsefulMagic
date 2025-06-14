package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.ClientManaManager
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * @param cost 每次施法消耗魔力数量
 * @param damage 每个弹幕的伤害
 */
open class WandItem(
    settings: Settings, val cost: Int, val damage: Double
) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(
            Text.of(
                Text.translatable(
                    "item.wand.damage"
                ).string.replace(
                    "%amount%", "$damage"
                )
            )
        )
        tooltip.add(
            Text.of(
                Text.translatable(
                    "item.wand.cost"
                ).string.replace("%cost%", "$cost")
            )
        )
        tooltip.add(
            Text.of(
                Text.translatable(
                    "item.wand.mana"
                ).string
                    .replace("%mana%", ClientManaManager.getSelfMana().mana.toString())
            )
        )
    }


    fun cost(player: ServerPlayerEntity) {
        if (player.isInCreativeMode) {
            return
        }
        val data = UsefulMagic.state.getDataFromServer(player.uuid)
        data.mana -= cost
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val stack = user.getStackInHand(hand)
        val canUse = if (!world.isClient) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            return TypedActionResult.fail(stack)
        }
        user.setCurrentHand(hand)
        return TypedActionResult.consume(stack)
    }
}