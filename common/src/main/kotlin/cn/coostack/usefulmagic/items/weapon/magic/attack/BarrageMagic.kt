package cn.coostack.usefulmagic.items.weapon.magic.attack

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.barrages.magic.BarrageMagicBarrage
import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class BarrageMagic(properties: Properties) : MagicItem(properties) {
    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        val damage = MagicHelper.getMagicDamage(wandStack)
        // 释放barrage
        world.playSound(
            null,
            ofFloored(shooter.position()),
            SoundEvents.FIRECHARGE_USE,
            SoundSource.PLAYERS,
            10.0f,
            3.0f
        )
        // 释放barrage
        val barrage =
            BarrageMagicBarrage(
                damage,
                shooter,
                shooter.eyePosition,
                world as ServerLevel
            )
        barrage.direction = shooter.forward
        BarrageManager.spawn(barrage)
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.barrage_magic"))
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
    }


}
