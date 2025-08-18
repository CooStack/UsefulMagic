package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isIn
import net.minecraft.tags.ItemTags
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.particles.barrages.wand.WoodenBarrage
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import kotlin.random.Random

class WoodenWand(settings: Properties) : WandItem(settings, 2, 4.0) {
    val chanceBurn = 0.1
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.wooden_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isIn(ItemTags.PLANKS)
    }


    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        // 判断魔力量是否足够
        val canUse = if (!world.isClientSide) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            user.stopUsingItem()
            return
        }
        if (world.isClientSide) {
            return
        }
        val tick = getUseDuration(stack, user) - remainingUseTicks
        if (tick % 10 != 0 || tick < 10) return
        stack.hurtAndBreak(1, world as ServerLevel, user as ServerPlayer) {
            world.playSound(
                null,
                ofFloored(user.position()),
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                5.0f,
                1.0f
            )
        }
        world.playSound(
            null,
            ofFloored(user.position()),
            SoundEvents.FIRECHARGE_USE,
            SoundSource.PLAYERS,
            10.0f,
            3.0f
        )
        val random = Random(System.currentTimeMillis())
        // 释放barrage
        val barrage =
            WoodenBarrage(damage, user, user.eyePosition, world as ServerLevel, random.nextInt(100) < 100 * chanceBurn)
        barrage.direction = user.forward
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
    }




    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20 * 3600
    }

}