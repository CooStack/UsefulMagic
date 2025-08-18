package cn.coostack.usefulmagic.items.consumer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class PurpleManaStar : Item(Properties()) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip.add(
            Component.translatable("item.purple_mana_star.description")
        )
    }


    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val stack = user.getItemInHand(hand)
        val data = if (world.isClientSide) {
            ClientManaManager.getSelfMana()
        } else {
            UsefulMagic.state.magicPlayerData[user.uuid]
        }
        val mana = data!!.maxMana

        if (mana !in 500..<1500) {
            return InteractionResultHolder.fail(stack)
        }

        if (!world.isClientSide) {
            // 增加上限
            world.playSound(
                null, user.x, user.y, user.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                3f, 2f
            )
        }
        data.maxMana += 50
        stack.count -= 1
        return super.use(world, user, hand)
    }

}