package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.ClientManaManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class RedManaCrystal : Item(Settings()) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(
            Text.translatable("item.red_mana_crystal.description")
        )
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val stack = user.getStackInHand(hand)
        val data = if (world.isClient) {
            ClientManaManager.getSelfMana()
        } else {
            UsefulMagic.state.playerManaData[user.uuid]
        }
        val mana = data!!.manaRegeneration

        if (mana !in 30 ..< 60) {
            return TypedActionResult.fail(stack)
        }

        if (!world.isClient) {
            // 增加上限
            world.playSound(
                null, user.x, user.y, user.z,
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                3f, 2f
            )
        }
        data.manaRegeneration += 3
        stack.decrement(1)
        user.itemCooldownManager.set(this, 10)
        return super.use(world, user, hand)
    }

}