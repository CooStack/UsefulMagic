package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.particles.barrages.wand.WoodenBarrage
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.tag.ItemTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.random.Random

class WoodenWand(settings: Settings) : WandItem(settings, 2, 4.0) {
    val chanceBurn = 0.1
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.wooden_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isIn(ItemTags.PLANKS)
    }


    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        // 判断魔力量是否足够
        val canUse = if (!world.isClient) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            user.clearActiveItem()
            return
        }
        if (world.isClient) {
            return
        }
        val tick = getMaxUseTime(stack, user) - remainingUseTicks
        if (tick % 10 != 0 || tick < 10) return
        stack.damage(1, world as ServerWorld, user as ServerPlayerEntity) {
            world.playSound(
                null,
                BlockPos.ofFloored(user.pos),
                SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS,
                5.0f,
                1.0f
            )
        }
        world.playSound(
            null,
            BlockPos.ofFloored(user.pos),
            SoundEvents.ITEM_FIRECHARGE_USE,
            SoundCategory.PLAYERS,
            10.0f,
            3.0f
        )
        val random = Random(System.currentTimeMillis())
        // 释放barrage
        val barrage =
            WoodenBarrage(damage, user, user.eyePos, world as ServerWorld, random.nextInt(100) < 100 * chanceBurn)
        barrage.direction = user.rotationVector
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 20 * 3600
    }

}